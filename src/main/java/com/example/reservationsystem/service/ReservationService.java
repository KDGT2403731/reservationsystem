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

		System.out.println(">>> [ReservationService] 予約作成開始");
		System.out.println("    Customer: " + customer.getEmail());
		System.out.println("    StaffId: " + staffId);
		System.out.println("    Date: " + date);
		System.out.println("    TimeSlot: " + timeSlot);
		System.out.println("    Menu: " + menu);

		// ★Step 1: スタッフを取得
		User staff = userRepository.findById(staffId)
				.orElseThrow(() -> new IllegalArgumentException("Staff not found"));
		System.out.println("✓ スタッフ取得: " + staff.getName());

		// ★Step 2: シフト存在確認
		boolean staffHasShift = shiftRepository.findByStaffAndDate(staff, date)
				.map(shift -> !timeSlot.isBefore(shift.getStartTime())
						&& !timeSlot.isAfter(shift.getEndTime().minusMinutes(1)))
				.orElse(false);

		if (!staffHasShift) {
			System.out.println("✗ スタッフはこの時間帯にシフトがありません");
			throw new IllegalStateException("Staff is not available at this time.");
		}
		System.out.println("✓ シフト確認完了");

		// ★Step 3: 予約枠の確認
		Optional<Reservation> existingReservation = reservationRepository.findByDateAndTimeSlotAndStaff(date, timeSlot,
				staff);
		if (existingReservation.isPresent()) {
			System.out.println("✗ この時間枠は既に予約済み");
			throw new IllegalStateException("This time slot is already booked.");
		}
		System.out.println("✓ 予約枠確認完了（空いています）");

		// ★Step 4: 予約オブジェクトを作成
		Reservation reservation = new Reservation();
		reservation.setUser(customer);
		reservation.setStaff(staff);
		reservation.setDate(date);
		reservation.setTimeSlot(timeSlot);
		reservation.setMenu(menu);
		reservation.setStatus("予約済");

		// ★Step 5: データベースに保存
		Reservation saved = reservationRepository.save(reservation);

		System.out.println("✓ 予約データベース保存完了");
		System.out.println("  - Reservation ID: " + saved.getId());
		System.out.println("  - Status: " + saved.getStatus());
		System.out.println(">>> [ReservationService] 予約作成完了\n");

		return saved;
	}

	@Transactional
	public Reservation updateReservation(Long reservationId, LocalDate newDate,
			LocalTime newTimeSlot, String newMenu) {

		System.out.println(">>> [ReservationService] 予約更新開始: ID=" + reservationId);

		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

		// ★新しい時間枠が既に予約されていないか確認
		if (reservationRepository.findByDateAndTimeSlotAndStaff(newDate, newTimeSlot, reservation.getStaff())
				.filter(r -> !r.getId().equals(reservationId))
				.isPresent()) {
			System.out.println("✗ 新しい時間枠は既に予約済み");
			throw new IllegalStateException("This new time slot is already booked.");
		}

		// ★スタッフがその日にシフトを持っているか確認
		boolean staffHasShift = shiftRepository.findByStaffAndDate(reservation.getStaff(), newDate)
				.map(shift -> !newTimeSlot.isBefore(shift.getStartTime())
						&& !newTimeSlot.isAfter(shift.getEndTime().minusMinutes(1)))
				.orElse(false);

		if (!staffHasShift) {
			System.out.println("✗ スタッフはこの新しい時間帯にシフトがありません");
			throw new IllegalStateException("Staff is not available at this new time.");
		}

		// ★更新
		reservation.setDate(newDate);
		reservation.setTimeSlot(newTimeSlot);
		reservation.setMenu(newMenu);

		Reservation updated = reservationRepository.save(reservation);
		System.out.println("✓ 予約更新完了\n");

		return updated;
	}

	@Transactional
	public void cancelReservation(Long reservationId) {

		System.out.println(">>> [ReservationService] 予約キャンセル開始: ID=" + reservationId);

		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

		reservation.setStatus("キャンセル済");
		reservationRepository.save(reservation);

		System.out.println("✓ 予約ステータスを『キャンセル済』に更新");

		LocalDate date = reservation.getDate();
		LocalTime timeSlot = reservation.getTimeSlot();
		User staff = reservation.getStaff();

		// ★この時間枠で待機中の顧客を探す
		List<Waitlist> candidates = waitlistRepository.findByWaitDateAndStartTimeAndStaffAndRequestStatus(
				date, timeSlot, staff, "PENDING");

		if (!candidates.isEmpty()) {
			System.out.println("✓ キャンセル待ち候補が見つかりました: " + candidates.size() + "件");

			Waitlist topCandidate = candidates.stream()
					.sorted(Comparator.comparing(Waitlist::getRequestedAt))
					.findFirst().get();

			// ★顧客へ通知を送る
			sendNotificationToCustomer(topCandidate.getUser().getId(), date, timeSlot);

			// ★待機録のステータスを『通知済』に更新
			topCandidate.setRequestStatus("NOTIFIED");
			topCandidate.setNotifiedAt(LocalDateTime.now());
			topCandidate.setExpirationTime(LocalDateTime.now().plusHours(1));
			waitlistRepository.save(topCandidate);

			System.out.println("✓ キャンセル待ち（ID=" + topCandidate.getId() + "）を『通知済』に更新");
		} else {
			System.out.println("ℹ キャンセル待ち候補なし");
		}

		System.out.println(">>> [ReservationService] 予約キャンセル完了\n");
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