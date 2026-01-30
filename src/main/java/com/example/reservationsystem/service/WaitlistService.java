package com.example.reservationsystem.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.reservationsystem.entity.Reservation;
import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.entity.Waitlist;
import com.example.reservationsystem.repository.ReservationRepository;
import com.example.reservationsystem.repository.UserRepository;
import com.example.reservationsystem.repository.WaitlistRepository;

@Service
public class WaitlistService {
	private final WaitlistRepository waitlistRepository;
	private final UserRepository userRepository;
	private final ReservationService reservationService;

	public WaitlistService(WaitlistRepository waitlistRepository,
			UserRepository userRepository,
			ReservationService reservationService,
			ReservationRepository reservationRepository) {
		this.waitlistRepository = waitlistRepository;
		this.userRepository = userRepository;
		this.reservationService = reservationService;
	}

	// ユーザーのキャンセル待ち一覧を取得
	public List<Waitlist> getUserWaitlists(User user, String status) {
		return waitlistRepository.findByUserAndRequestStatus(user, status);
	}

	// 日付でキャンセル待ちを検索
	public List<Waitlist> getWaitlistsByDate(LocalDate date) {
		return waitlistRepository.findByWaitDate(date);
	}

	// 全キャンセル待ちを取得
	public List<Waitlist> getAllWaitlists() {
		return waitlistRepository.findAll();
	}

	// キャンセル待ち登録
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

	// キャンセル待ち削除（顧客による取り消し）
	@Transactional
	public void cancelWaitlistByCustomer(Long waitlistId, User customer) {
		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		if (!waitlist.getUser().getId().equals(customer.getId())) {
			throw new IllegalStateException("他の顧客のキャンセル待ちは取り消せません。");
		}

		waitlistRepository.delete(waitlist);
	}

	// キャンセル待ち削除（スタッフによる取り消し）
	@Transactional
	public void cancelWaitlistByStaff(Long waitlistId) {
		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		waitlistRepository.delete(waitlist);
	}

	// ★★★ 重要 ★★★ キャンセル待ちから予約確定（顧客による確定）
	// 修正点：Waitlistを先に削除してから予約を作成することで、
	//        同じ時間枠の重複チェックを回避する
	@Transactional
	public void confirmReservationFromWaitlist(Long waitlistId, User customer) {
		System.out.println(">>> [WaitlistService] キャンセル待ちから予約確定開始: WaitlistId=" + waitlistId);

		// Step 1: 待機録を取得
		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		System.out.println("✓ Waitlist取得: ID=" + waitlist.getId());

		// Step 2: 顧客の確認
		if (!waitlist.getUser().getId().equals(customer.getId())) {
			throw new IllegalStateException("他の顧客のキャンセル待ちは確定できません。");
		}

		System.out.println("✓ 顧客確認: " + customer.getEmail());

		// Step 3: ステータス確認
		if (!"NOTIFIED".equals(waitlist.getRequestStatus())) {
			throw new IllegalStateException("通知済みのキャンセル待ちのみ確定できます。");
		}

		System.out.println("✓ ステータス確認: NOTIFIED");

		// Step 4: 確認期限チェック
		if (waitlist.getExpirationTime() != null &&
				LocalDateTime.now().isAfter(waitlist.getExpirationTime())) {
			waitlist.setRequestStatus("EXPIRED");
			waitlistRepository.save(waitlist);
			throw new IllegalStateException("確認期限が過ぎています。");
		}

		System.out.println("✓ 期限確認: 有効");

		// ★★★ 修正点: Waitlist を先に削除
		// これにより、新規予約作成時の重複チェックで、この Waitlist が検出されなくなる
		waitlistRepository.delete(waitlist);
		System.out.println("✓ Waitlist削除完了");

		// Step 5: 新規予約を作成（別トランザクションで実行）
		try {
			Reservation newReservation = createReservationInNewTransaction(
					customer,
					waitlist.getStaff().getId(),
					waitlist.getWaitDate(),
					waitlist.getStartTime(),
					waitlist.getReservationMenu());

			System.out.println("✓ 予約作成成功: Reservation ID=" + newReservation.getId());

			// Step 6: 予約が実際に保存されたか確認
			if (newReservation == null || newReservation.getId() == null) {
				throw new IllegalStateException("予約の保存に失敗しました。");
			}

			System.out.println("✓ キャンセル待ちから予約確定完了");
			System.out.println("  - Customer: " + customer.getEmail());
			System.out.println("  - Date: " + newReservation.getDate() + " " + newReservation.getTimeSlot());
			System.out.println(">>> [WaitlistService] 処理完了\n");

		} catch (IllegalStateException e) {
			System.out.println("✗ 予約作成失敗: " + e.getMessage());
			// Waitlist は既に削除されているため、ここで復旧することは難しい
			// ログに記録し、管理画面で対応することを推奨
			throw e;
		}
	}

	// ★★★ 新しいメソッド ★★★
	// 別トランザクションで予約を作成
	// @Transactional(propagation = Propagation.REQUIRES_NEW) により、
	// 親トランザクションと独立した新しいトランザクションを開始する
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private Reservation createReservationInNewTransaction(User customer, Long staffId,
			LocalDate date, LocalTime timeSlot, String menu) {
		return reservationService.createReservation(customer, staffId, date, timeSlot, menu);
	}

	// キャンセル待ちから予約確定（スタッフによる確定）
	@Transactional
	public void confirmReservationByStaff(Long waitlistId) {
		System.out.println(">>> [WaitlistService] スタッフによるキャンセル待ち確定開始: WaitlistId=" + waitlistId);

		Waitlist waitlist = waitlistRepository.findById(waitlistId)
				.orElseThrow(() -> new IllegalArgumentException("Waitlist not found"));

		System.out.println("✓ Waitlist取得: ID=" + waitlist.getId());

		// ★★★ 修正点: Waitlist を先に削除
		waitlistRepository.delete(waitlist);
		System.out.println("✓ Waitlist削除完了");

		// Step 2: 新規予約を作成（別トランザクションで実行）
		try {
			Reservation newReservation = createReservationInNewTransaction(
					waitlist.getUser(),
					waitlist.getStaff().getId(),
					waitlist.getWaitDate(),
					waitlist.getStartTime(),
					waitlist.getReservationMenu());

			System.out.println("✓ 予約作成成功: Reservation ID=" + newReservation.getId());

			// Step 3: 予約が実際に保存されたか確認
			if (newReservation == null || newReservation.getId() == null) {
				throw new IllegalStateException("予約の保存に失敗しました。");
			}

			System.out.println("✓ スタッフによるキャンセル待ち確定完了");
			System.out.println(">>> [WaitlistService] 処理完了\n");

		} catch (IllegalStateException e) {
			System.out.println("✗ 予約作成失敗: " + e.getMessage());
			throw e;
		}
	}

	// キャンセル待ちに手動通知
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

	// 顧客への通知送信（ダミー実装）
	private void sendNotificationToCustomer(Long userId, LocalDate date, LocalTime timeSlot) {
		System.out.println("--- [WAITLIST NOTIFICATION] ---");
		System.out.println("User ID: " + userId + " is notified.");
		System.out.println("Available slot: " + date + " at " + timeSlot);
		System.out.println("---------------------------------");
	}
}