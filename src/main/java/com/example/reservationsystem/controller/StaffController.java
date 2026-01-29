package com.example.reservationsystem.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.reservationsystem.entity.Reservation;
import com.example.reservationsystem.entity.Shift;
import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.repository.ReservationRepository;
import com.example.reservationsystem.repository.UserRepository;
import com.example.reservationsystem.service.ReservationService;
import com.example.reservationsystem.service.ShiftService;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffController {
	private final ReservationService reservationService;
	private final ShiftService shiftService;
	private final UserRepository userRepository;
	private final ReservationRepository reservationRepository;

	public StaffController(ReservationService reservationService, ShiftService shiftService,
			UserRepository userRepository, ReservationRepository reservationRepository) {
		this.reservationService = reservationService;
		this.shiftService = shiftService;
		this.userRepository = userRepository;
		this.reservationRepository = reservationRepository;
	}

	@GetMapping("/reservations")
	public String listStaffReservations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User staff = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Staff not found"));
		model.addAttribute("staffReservations", reservationRepository.findByStaffOrderByDateDescTimeSlotDesc(staff));
		return "staff_reservations";
	}

	@GetMapping("/reservations/{id}/edit")
	public String showEditReservationFormByStaff(@PathVariable("id") Long reservationId, Model model) {
		Reservation reservation = reservationService.getReservationById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + reservationId));
		model.addAttribute("reservation", reservation);
		model.addAttribute("staffs", reservationService.getAllStaffs());
		return "staff_reservation_form";
	}

	@PostMapping("/reservations/{id}/edit")
	public String updateReservationByStaff(
			@PathVariable("id") Long reservationId,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam("timeSlot") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeSlot,
			@RequestParam("menu") String menu,
			Model model) {
		try {
			reservationService.updateReservation(reservationId, date, timeSlot, menu);
			return "redirect:/staff/reservations?success=updated";
		} catch (IllegalStateException e) {
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("reservation",
					reservationService.getReservationById(reservationId).orElse(new Reservation()));
			model.addAttribute("staffs", reservationService.getAllStaffs());
			return "reservation_form";
		}
	}

	@PostMapping("/reservations/{id}/cancel")
	public String cancelReservationByStaff(@PathVariable("id") Long reservationId) {
		reservationService.cancelReservation(reservationId);
		return "redirect:/staff/reservations?success=cancelled";
	}

	@GetMapping("/shifts")
	public String showStaffShifts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User staff = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Staff not found"));

		List<Shift> myShifts = shiftService.getShiftsByStaffId(staff.getId());

		model.addAttribute("staffShifts", myShifts);
		model.addAttribute("currentStaff", staff);

		return "staff_shift_management";
	}

	// スタッフが自分のシフトを登録・更新
	@PostMapping("/shifts")
	public String createOrUpdateStaffShift(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
			@RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {

		User staff = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Staff not found"));

		shiftService.createOrUpdateShift(staff.getId(), date, startTime, endTime);
		return "redirect:/staff/shifts?success=shiftUpdated";
	}

	// スタッフが自分のシフトを削除
	@PostMapping("/shifts/{id}/delete")
	public String deleteStaffShift(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable("id") Long shiftId) {

		shiftService.deleteShift(shiftId);
		return "redirect:/staff/shifts?success=shiftDeleted";
	}
}
