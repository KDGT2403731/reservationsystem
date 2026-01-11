package com.example.reservationsystem.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reservationsystem.entity.Reservation;
import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.repository.UserRepository;
import com.example.reservationsystem.service.ReservationService;
import com.example.reservationsystem.service.ShiftService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	private final ReservationService reservationService;
	private final ShiftService shiftService;
	private final UserRepository userRepository;

	public AdminController(ReservationService reservationService, ShiftService shiftService,
			UserRepository userRepository) {
		this.reservationService = reservationService;
		this.shiftService = shiftService;
		this.userRepository = userRepository;
	}

	@GetMapping("/reservations")
	public String listAllReservations(
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			Model model) {
		List<Reservation> reservations;
		if (startDate != null && endDate != null) {
			reservations = reservationService.getReservationsByDateRange(startDate, endDate);
		} else {
			reservations = reservationService.getAllReservations();
		}
		model.addAttribute("allReservations", reservations);
		return "admin_reservations";
	}

	@GetMapping("/shifts")
	public String listShifts(Model model) {
		// 1. シフト登録のプルダウン用に全スタッフを取得 (ROLE_STAFFのみ)
		// userRepositoryにfindByRoleメソッドがある前提、またはfindAllをフィルタリング
		List<User> staffs = userRepository.findAll().stream()
				.filter(u -> "STAFF".equals(u.getRole()))
				.toList();
		model.addAttribute("staffs", staffs);

		// 2. 一覧表示用に全スタッフのシフトを取得
		model.addAttribute("allShifts", shiftService.getAllShifts());

		return "admin_shifts";
	}

	@PostMapping("/shifts/create-update")
	public String createOrUpdateShiftByAdmin(
			@RequestParam("staffId") Long staffId,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
		shiftService.createOrUpdateShift(staffId, date, startTime, endTime);
		return "redirect:/admin/shifts?success=shiftUpdated";
	}

	@PostMapping("/shifts/{id}/delete")
	public String deleteShiftByAdmin(@PathVariable("id") Long shiftId) {
		shiftService.deleteShift(shiftId);
		return "redirect:/admin/shifts?success=shiftDeleted";
	}

	@GetMapping("/statistics")
	public String showStatistics(
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			Model model) {
		if (startDate == null)
			startDate = LocalDate.now().minusMonths(1);
		if (endDate == null)
			endDate = LocalDate.now();
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("reservationCountByMenu",
				reservationService.getReservationCountByMenu(startDate, endDate));
		model.addAttribute("reservationCountByStaff",
				reservationService.getReservationCountByStaff(startDate, endDate));
		return "admin_statistics";
	}

	@GetMapping("/statistics/csv")
	public void exportStatisticsCsv(
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			HttpServletResponse response) throws IOException {
		if (startDate == null)
			startDate = LocalDate.now().minusMonths(1);
		if (endDate == null)
			endDate = LocalDate.now();

		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"reservation_statistics.csv\"");

		try (PrintWriter writer = response.getWriter()) {
			writer.append("統計期間: " + startDate + "から" + endDate + "\n\n");
			writer.append("メニュー別予約数\n");
			reservationService.getReservationCountByMenu(startDate, endDate).forEach((menu, count) -> {
				writer.append(menu + "," + count + "\n");
			});
			// 区切りの空行
			writer.append("\n スタッフ別予約数\n");
			// スタッフ別件数を同様に出力（staff,count）
			reservationService.getReservationCountByStaff(startDate, endDate).forEach((staff, count) -> {
				writer.append(staff + "," + count + "\n");
			});
		}
	}
}
