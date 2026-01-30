package com.example.reservationsystem.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.entity.Waitlist;
import com.example.reservationsystem.repository.UserRepository;
import com.example.reservationsystem.repository.WaitlistRepository;
import com.example.reservationsystem.service.ReservationService;
import com.example.reservationsystem.service.WaitlistService;

@Controller
public class WaitlistController {
	private final WaitlistService waitlistService;
	private final WaitlistRepository waitlistRepository;
	private final UserRepository userRepository;
	private final ReservationService reservationService;

	public WaitlistController(WaitlistService waitlistService,
			WaitlistRepository waitlistRepository,
			UserRepository userRepository,
			ReservationService reservationService) {
		this.waitlistService = waitlistService;
		this.waitlistRepository = waitlistRepository;
		this.userRepository = userRepository;
		this.reservationService = reservationService;
	}

	// ========== 顧客用機能 ==========

	@GetMapping("/waitlist/register")
	public String showWaitlistRegisterForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User customer = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		model.addAttribute("staffs", reservationService.getAllStaffs());

		List<Waitlist> userWaitlists = waitlistService.getUserWaitlists(customer, "PENDING");
		List<Waitlist> notifiedWaitlists = waitlistService.getUserWaitlists(customer, "NOTIFIED");
		List<Waitlist> expiredWaitlists = waitlistService.getUserWaitlists(customer, "EXPIRED");

		userWaitlists.addAll(notifiedWaitlists);
		userWaitlists.addAll(expiredWaitlists);
		userWaitlists.sort((w1, w2) -> w2.getRequestedAt().compareTo(w1.getRequestedAt()));

		model.addAttribute("userWaitlists", userWaitlists);

		return "waitlist_register";
	}

	@PostMapping("/waitlist/register")
	public String registerWaitlist(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("staffId") Long staffId,
			@RequestParam("waitDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate waitDate,
			@RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam("reservationMenu") String reservationMenu,
			Model model) {

		try {
			User customer = userRepository.findByEmail(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("Customer not found"));

			waitlistService.registerWaitlist(customer, staffId, waitDate, startTime, reservationMenu);

			return "redirect:/waitlist/register?success=registered";

		} catch (IllegalStateException | IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("staffs", reservationService.getAllStaffs());
			return "waitlist_register";
		}
	}

	@PostMapping("/waitlist/{id}/cancel")
	public String cancelWaitlistByCustomer(
			@PathVariable("id") Long waitlistId,
			@AuthenticationPrincipal UserDetails userDetails) {

		User customer = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		waitlistService.cancelWaitlistByCustomer(waitlistId, customer);

		return "redirect:/waitlist/register?success=cancelled";
	}

	// ☆重要☆ キャンセル待ちから予約を確定するエンドポイント
	//
	// 流れ：
	// 1. waitlist/{id}/confirm を GET リクエスト
	// 2. WaitlistService.confirmReservationFromWaitlist() を実行
	// 3. 成功時：/reservation/history にリダイレクト
	// 4. 失敗時：/waitlist/register にエラー付きでリダイレクト
	//
	@GetMapping("/waitlist/{id}/confirm")
	public String confirmReservationFromWaitlist(
			@PathVariable("id") Long waitlistId,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		System.out.println(">>> [WaitlistController] 予約確定要求: WaitlistId=" + waitlistId);

		try {
			User customer = userRepository.findByEmail(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("Customer not found"));

			System.out.println("✓ 顧客確認: " + customer.getEmail());

			// ☆キャンセル待ちから予約を確定（トランザクション内で実行）
			waitlistService.confirmReservationFromWaitlist(waitlistId, customer);

			System.out.println("✓ 予約確定成功\n");

			// ☆成功時：予約履歴ページにリダイレクト
			return "redirect:/reservation/history?success=confirmed";

		} catch (IllegalStateException e) {
			System.out.println("✗ 予約確定失敗 (IllegalStateException): " + e.getMessage());
			try {
				String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString());
				return "redirect:/waitlist/register?error=" + encodedError;
			} catch (UnsupportedEncodingException ex) {
				return "redirect:/waitlist/register?error=An error occurred";
			}

		} catch (IllegalArgumentException e) {
			System.out.println("✗ 予約確定失敗 (IllegalArgumentException): " + e.getMessage());
			try {
				String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString());
				return "redirect:/waitlist/register?error=" + encodedError;
			} catch (UnsupportedEncodingException ex) {
				return "redirect:/waitlist/register?error=Invalid waitlist ID";
			}

		} catch (Exception e) {
			System.out.println("✗ 予約確定失敗 (予期しないエラー): " + e.getClass().getSimpleName() + " - " + e.getMessage());
			e.printStackTrace();
			return "redirect:/waitlist/register?error=Unexpected error occurred";
		}
	}

	// ========== スタッフ用機能 ==========

	@GetMapping("/staff/waitlist")
	@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
	public String showWaitlistManagement(
			@RequestParam(value = "date", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate filterDate,
			@RequestParam(value = "status", required = false) String filterStatus,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) {

		User currentStaff = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Staff not found"));

		List<Waitlist> waitlists;

		if (filterDate != null) {
			waitlists = waitlistService.getWaitlistsByDate(filterDate);
		} else {
			waitlists = waitlistService.getAllWaitlists();
		}

		if (filterStatus != null && !filterStatus.isEmpty()) {
			waitlists = waitlists.stream()
					.filter(w -> filterStatus.equals(w.getRequestStatus()))
					.collect(Collectors.toList());
		}

		if ("STAFF".equals(currentStaff.getRole())) {
			final Long currentStaffId = currentStaff.getId();
			waitlists = waitlists.stream()
					.filter(w -> w.getStaff() != null && currentStaffId.equals(w.getStaff().getId()))
					.collect(Collectors.toList());
		}

		waitlists.sort((w1, w2) -> w1.getRequestedAt().compareTo(w2.getRequestedAt()));

		waitlists.forEach(w -> {
			if ("NOTIFIED".equals(w.getRequestStatus()) && w.getExpirationTime() != null) {
				Duration duration = Duration.between(LocalDateTime.now(), w.getExpirationTime());
				long minutes = duration.toMinutes();

				if (minutes > 0) {
					long hours = minutes / 60;
					long mins = minutes % 60;
					w.setTimeRemaining(String.format("残り%d時間%d分", hours, mins));
					w.setExpiringSoon(minutes < 30);
				} else {
					w.setRequestStatus("EXPIRED");
					waitlistRepository.save(w);
				}
			}
		});

		long pendingCount = waitlists.stream()
				.filter(w -> "PENDING".equals(w.getRequestStatus())).count();
		long notifiedCount = waitlists.stream()
				.filter(w -> "NOTIFIED".equals(w.getRequestStatus())).count();
		long expiredCount = waitlists.stream()
				.filter(w -> "EXPIRED".equals(w.getRequestStatus())).count();

		model.addAttribute("waitlistStats", new WaitlistStats(pendingCount, notifiedCount, expiredCount));
		model.addAttribute("waitlists", waitlists);

		return "staff_waitlist_management";
	}

	@PostMapping("/staff/waitlist/{id}/notify")
	public String notifyCustomer(@PathVariable("id") Long waitlistId) {
		waitlistService.notifyCustomer(waitlistId);
		return "redirect:/staff/waitlist?success=notified";
	}

	@PostMapping("/staff/waitlist/{id}/cancel")
	public String cancelWaitlistByStaff(@PathVariable("id") Long waitlistId) {
		waitlistService.cancelWaitlistByStaff(waitlistId);
		return "redirect:/staff/waitlist?success=cancelled";
	}

	// ☆スタッフによるキャンセル待ちから予約確定
	@GetMapping("/staff/waitlist/{id}/confirm")
	public String confirmReservationByStaff(
			@PathVariable("id") Long waitlistId,
			Model model) {
		try {
			System.out.println(">>> [WaitlistController] スタッフが予約確定要求: WaitlistId=" + waitlistId);
			waitlistService.confirmReservationByStaff(waitlistId);
			System.out.println("✓ スタッフによる予約確定成功\n");
			return "redirect:/staff/reservations?success=confirmed";
		} catch (IllegalStateException e) {
			System.out.println("✗ スタッフによる予約確定失敗: " + e.getMessage());
			try {
				String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString());
				return "redirect:/staff/waitlist?error=" + encodedError;
			} catch (UnsupportedEncodingException ex) {
				return "redirect:/staff/waitlist?error=An error occurred";
			}
		} catch (Exception e) {
			System.out.println("✗ スタッフによる予約確定失敗 (予期しないエラー): " + e.getMessage());
			e.printStackTrace();
			return "redirect:/staff/waitlist?error=Unexpected error occurred";
		}
	}

	// ========== 内部クラス（統計情報の保持用） ==========

	public static class WaitlistStats {
		private final long pending;
		private final long notified;
		private final long expired;

		public WaitlistStats(long pending, long notified, long expired) {
			this.pending = pending;
			this.notified = notified;
			this.expired = expired;
		}

		public long getPending() {
			return pending;
		}

		public long getNotified() {
			return notified;
		}

		public long getExpired() {
			return expired;
		}
	}
}