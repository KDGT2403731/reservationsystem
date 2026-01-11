package com.example.reservationsystem.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.entity.Waitlist;
import com.example.reservationsystem.repository.UserRepository;
import com.example.reservationsystem.repository.WaitlistRepository;

@Service
public class WaitlistService {
	private final WaitlistRepository waitlistRepository;
	private final UserRepository userRepository;
	private final ReservationService reservationService;

	public WaitlistService(WaitlistRepository waitlistRepository,
			UserRepository userRepository,
			ReservationService reservationService) {
		this.waitlistRepository = waitlistRepository;
		this.userRepository = userRepository;
		this.reservationService = reservationService;
	}

	/**
	 * ユーザーのキャンセル待ち一覧を取得
	 */
	public List<Waitlist> getUserWaitlists(User user, String status) {
		return waitlistRepository.findByUserAndRequestStatus(user, status);
	}

	/**
	 * 日付でキャンセル待ちを検索
	 */
	public List<Waitlist> getWaitlistsByDate(LocalDate date) {
		return waitlistRepository.findByWaitDate(date);
	}

	/**
	 * 全キャンセル待ちを取得
	 */
	public List<Waitlist> getAllWaitlists() {
		return waitlistRepository.findAll();
	}

	/**
	 * キャンセル待ち登録
	 */
	@Transactional
	public Waitlist registerWaitlist(User customer, Long staffId, LocalDate waitDate,
			LocalTime startTime, String reservationMenu) {

		User staff = userRepository.findById(staffId)
				.orElseThrow(() -> new IllegalArgumentException("Staff not found"));

		if (waitDate.isBefore(LocalDate.now())) {
			throw new IllegalStateException("過去の日付は選択できません。");
		}

		List<Waitlist> existingWaitlists = waitlistRepository
				.findByWaitDateAndStartTimeAndStaffAndRequestStatus(waitDate, startTime, staff, "PENDING");

		boolean alreadyRegistered = existingWaitlists.stream()
				.anyMatch(w -> w.getUser().getId().equals(customer.getId()));

		if (alreadyRegistered) {
			throw new IllegalStateException("既に同じ条件でキャンセル待ちを登録しています。");
		}

		Waitlist waitlist = new Waitlist();
		waitlist.setUser(customer);
		waitlist.setStaff(staff);
		waitlist.setWaitDate(waitDate);
		waitlist.setStartTime(startTime);
		waitlist.setReservationMenu(reservationMenu);
		waitlist.setRequestStatus("PENDING");
		waitlist.setRequestedAt(LocalDateTime.now());

		return waitlistRepository.save(waitlist);
	}

	/**
	 * キャンセル待ち削除（顧客による取り消し）
	 */
	@Transactional
	public void cancelWaitlistByCustomer(Long waitlistId, User customer) {
		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		if (!waitlist.getUser().getId().equals(customer.getId())) {
			throw new IllegalStateException("他の顧客のキャンセル待ちは取り消せません。");
		}

		waitlistRepository.delete(waitlist);
	}

	/**
	 * キャンセル待ち削除（スタッフによる取り消し）
	 */
	@Transactional
	public void cancelWaitlistByStaff(Long waitlistId) {
		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		waitlistRepository.delete(waitlist);
	}

	/**
	 * キャンセル待ちから予約確定（顧客による確定）
	 */
	@Transactional
	public void confirmReservationFromWaitlist(Long waitlistId, User customer) {
		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		if (!waitlist.getUser().getId().equals(customer.getId())) {
			throw new IllegalStateException("他の顧客のキャンセル待ちは確定できません。");
		}

		if (!"NOTIFIED".equals(waitlist.getRequestStatus())) {
			throw new IllegalStateException("通知済みのキャンセル待ちのみ確定できます。");
		}

		if (waitlist.getExpirationTime() != null &&
				LocalDateTime.now().isAfter(waitlist.getExpirationTime())) {
			waitlist.setRequestStatus("EXPIRED");
			waitlistRepository.save(waitlist);
			throw new IllegalStateException("確定期限が過ぎています。");
		}

		reservationService.createReservation(
				customer,
				waitlist.getStaff().getId(),
				waitlist.getWaitDate(),
				waitlist.getStartTime(),
				waitlist.getReservationMenu());

		waitlistRepository.delete(waitlist);
	}

	/**
	 * キャンセル待ちから予約確定（スタッフによる確定）
	 */
	@Transactional
	public void confirmReservationByStaff(Long waitlistId) {
		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		reservationService.createReservation(
				waitlist.getUser(),
				waitlist.getStaff().getId(),
				waitlist.getWaitDate(),
				waitlist.getStartTime(),
				waitlist.getReservationMenu());

		waitlistRepository.delete(waitlist);
	}

	/**
	 * キャンセル待ちに手動通知
	 */
	@Transactional
	public void notifyCustomer(Long waitlistId) {
		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		if (!"PENDING".equals(waitlist.getRequestStatus())) {
			throw new IllegalStateException("待機中のキャンセル待ちのみ通知できます。");
		}

		sendNotificationToCustomer(waitlist.getUser().getId(), waitlist.getWaitDate(), waitlist.getStartTime());

		waitlist.setRequestStatus("NOTIFIED");
		waitlist.setNotifiedAt(LocalDateTime.now());
		waitlist.setExpirationTime(LocalDateTime.now().plusHours(1));

		waitlistRepository.save(waitlist);
	}

	/**
	 * 顧客への通知送信（ダミー実装）
	 */
	private void sendNotificationToCustomer(Long userId, LocalDate date, LocalTime timeSlot) {
		System.out.println("--- [WAITLIST NOTIFICATION] ---");
		System.out.println("User ID: " + userId + " is notified.");
		System.out.println("Available slot: " + date + " at " + timeSlot);
		System.out.println("---------------------------------");
	}
}