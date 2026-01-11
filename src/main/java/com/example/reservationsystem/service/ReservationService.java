package com.example.reservationsystem.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reservationsystem.entity.Reservation;
import com.example.reservationsystem.entity.Shift;
import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.entity.Waitlist;
import com.example.reservationsystem.repository.ReservationRepository;
import com.example.reservationsystem.repository.ShiftRepository;
import com.example.reservationsystem.repository.UserRepository;
import com.example.reservationsystem.repository.WaitlistRepository;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final UserRepository userRepository;
	private final ShiftRepository shiftRepository;
	private final WaitlistRepository waitlistRepository;

	public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository,
			ShiftRepository shiftRepository, WaitlistRepository waitlistRepository) {
		this.reservationRepository = reservationRepository;
		this.userRepository = userRepository;
		this.shiftRepository = shiftRepository;
		this.waitlistRepository = waitlistRepository;
	}

	public List<Reservation> getUserReservations(User user) {
		return reservationRepository.findByUserOrderByDateDescTimeSlotDesc(user);
	}

	public Optional<Reservation> getReservationById(Long id) {
		return reservationRepository.findById(id);
	}

	public List<Reservation> getAllReservations() {
		return reservationRepository.findAll();
	}

	public List<Reservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
		return reservationRepository.findByDateBetween(startDate, endDate);
	}

	@Transactional
	public Reservation createReservation(User customer, Long staffId, LocalDate date,
			LocalTime timeSlot, String menu) {
		User staff = userRepository.findById(staffId)
				.orElseThrow(() -> new IllegalArgumentException("Staff not found"));
		boolean staffHasShift = shiftRepository.findByStaffAndDate(staff, date)
				.map(shift -> !timeSlot.isBefore(shift.getStartTime())
						&& !timeSlot.isAfter(shift.getEndTime().minusMinutes(1)))
				.orElse(false);
		if (!staffHasShift) {
			throw new IllegalStateException("Staff is not available at this time.");
		}

		if (reservationRepository.findByDateAndTimeSlotAndStaff(date, timeSlot, staff).isPresent()) {
			throw new IllegalStateException("This time slot is already booked.");
		}
		Reservation reservation = new Reservation();
		reservation.setUser(customer);
		reservation.setStaff(staff);
		reservation.setDate(date);
		reservation.setTimeSlot(timeSlot);
		reservation.setMenu(menu);
		reservation.setStatus("予約済");
		return reservationRepository.save(reservation);
	}

	@Transactional
	public Reservation updateReservation(Long reservationId, LocalDate newDate,
			LocalTime newTimeSlot, String newMenu) {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
		if (reservationRepository.findByDateAndTimeSlotAndStaff(newDate, newTimeSlot, reservation.getStaff())
				.filter(r -> !r.getId().equals(reservationId))
				.isPresent()) {
			throw new IllegalStateException("This new time slot is already booked.");
		}

		boolean staffHasShift = shiftRepository.findByStaffAndDate(reservation.getStaff(), newDate)
				.map(shift -> !newTimeSlot.isBefore(shift.getStartTime())
						&& !newTimeSlot.isAfter(shift.getEndTime().minusMinutes(1)))
				.orElse(false);
		if (!staffHasShift) {
			throw new IllegalStateException("Staff is not available at this new time.");
		}
		reservation.setDate(newDate);
		reservation.setTimeSlot(newTimeSlot);
		reservation.setMenu(newMenu);
		return reservationRepository.save(reservation);
	}

	@Transactional
	public void cancelReservation(Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
		reservation.setStatus("キャンセル済");
		reservationRepository.save(reservation);

		LocalDate date = reservation.getDate();
		LocalTime timeSlot = reservation.getTimeSlot();
		User staff = reservation.getStaff();

		List<Waitlist> candidates = waitlistRepository.findByWaitDateAndStartTimeAndStaffAndRequestStatus(
				date, timeSlot, staff, "PENDING");

		if (!candidates.isEmpty()) {
			Waitlist topCandidate = candidates.stream()
					.sorted(Comparator.comparing(Waitlist::getRequestedAt))
					.findFirst().get();

			// 3. 顧客へLINE通知を送る（ダミー処理）
			// 実際にはLINE APIを呼び出す外部連携サービスが必要です。
			sendNotificationToCustomer(topCandidate.getUser().getId(), date, timeSlot);

			// 4. ウェイティングリストのステータスを更新
			topCandidate.setRequestStatus("NOTIFIED");
			topCandidate.setNotifiedAt(LocalDateTime.now());
			// 通知期限 (例: 1時間後) を設定
			topCandidate.setExpirationTime(LocalDateTime.now().plusHours(1));
			waitlistRepository.save(topCandidate);
		}
	}

	public List<User> getAllStaffs() {
		return userRepository.findByRole("STAFF");
	}

	public List<LocalTime> getAvailableTimeSlots(Long staffId, LocalDate date) {
		User staff = userRepository.findById(staffId)
				.orElseThrow(() -> new IllegalArgumentException("Staff not found"));

		Optional<Shift> staffShift = shiftRepository.findByStaffAndDate(staff, date);
		if (staffShift.isEmpty()) {
			return List.of();
		}

		LocalTime shiftStart = staffShift.get().getStartTime();
		LocalTime shiftEnd = staffShift.get().getEndTime();

		List<LocalTime> allPossibleSlots = generateTimeSlots(shiftStart, shiftEnd, 30);
		List<Reservation> bookedSlots = reservationRepository.findByStaffAndDateBetween(staff, date, date);
		return allPossibleSlots.stream()
				.filter(slot -> bookedSlots.stream().noneMatch(res -> res.getTimeSlot().equals(slot)))
				.collect(Collectors.toList());
	}

	private List<LocalTime> generateTimeSlots(LocalTime start, LocalTime end, int intervalMinutes) {
		List<LocalTime> slots = new ArrayList<>();
		LocalTime current = start;
		while (current.isBefore(end)) {
			slots.add(current);
			current = current.plusMinutes(intervalMinutes);
		}
		return slots;
	}

	public Map<String, Long> getReservationCountByMenu(LocalDate startDate, LocalDate endDate) {
		List<Reservation> reservations = reservationRepository.findByDateBetween(startDate, endDate);
		return reservations.stream()
				.collect(Collectors.groupingBy(Reservation::getMenu, Collectors.counting()));
	}

	public Map<String, Long> getReservationCountByStaff(LocalDate startDate, LocalDate endDate) {
		List<Reservation> reservations = reservationRepository.findByDateBetween(startDate, endDate);
		return reservations.stream()
				.filter(r -> r.getStaff() != null)
				.collect(Collectors.groupingBy(r -> r.getStaff().getName(), Collectors.counting()));
	}

	private void sendNotificationToCustomer(Long userId, LocalDate date, LocalTime timeSlot) {
		System.out.println("--- [WAITLIST NOTIFICATION] ---");
		System.out.println("User ID: " + userId + " is notified.");
		System.out.println("Available slot: " + date + " at " + timeSlot);
		System.out.println("---------------------------------");
	}
}
