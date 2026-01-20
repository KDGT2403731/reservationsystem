package com.example.reservationsystem.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.reservationsystem.entity.Reservation;
import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.repository.UserRepository;
import com.example.reservationsystem.service.ReservationService;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
	private final ReservationService reservationService;
	private final UserRepository userRepository;

	public ReservationController(ReservationService reservationService, UserRepository userRepository) {
		this.reservationService = reservationService;
		this.userRepository = userRepository;
	}

	@GetMapping("/new")
	public String showReservationForm(Model model) {
		model.addAttribute("staffs", reservationService.getAllStaffs());
		model.addAttribute("reservation", new Reservation());
		return "reservation_form";
	}

	@PostMapping("/new")
	public String createReservation(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("staffId") Long staffId,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam("timeSlot") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeSlot,
			@RequestParam("menu") String menu,
			Model model) {
		User customer = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Customer not found"));
		try {
			reservationService.createReservation(customer, staffId, date, timeSlot, menu);
			return "redirect:/reservation/history?success=created";
		} catch (IllegalStateException e) {
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("staffs", reservationService.getAllStaffs());
			Reservation tempReservation = new Reservation();
			tempReservation.setStaff(userRepository.findById(staffId).orElse(null));
			tempReservation.setDate(date);
			tempReservation.setTimeSlot(timeSlot);
			tempReservation.setMenu(menu);
			model.addAttribute("reservation", tempReservation);
			return "reservation_form";
		}
	}

	@GetMapping("/history")
	public String showReservationHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User customer = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Customer not found"));
		model.addAttribute("userReservations", reservationService.getUserReservations(customer));
		return "reservation_history";
	}

	@GetMapping("/{id}/edit")
	public String showEditReservationForm(@PathVariable("id") Long reservationId, Model model) {
		Reservation reservation = reservationService.getReservationById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + reservationId));
		model.addAttribute("reservation", reservation);
		model.addAttribute("staffs", reservationService.getAllStaffs());
		return "reservation_form";
	}

	@PostMapping("/{id}/edit")
	public String updateReservation(
			@PathVariable("id") Long reservationId,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam("timeSlot") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeSlot,
			@RequestParam("menu") String menu,
			Model model) {
		try {
			reservationService.updateReservation(reservationId, date, timeSlot, menu);
			return "redirect:/reservation/history?success=updated";
		} catch (IllegalStateException e) {
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("reservation",
					reservationService.getReservationById(reservationId).orElse(new Reservation()));
			model.addAttribute("staffs", reservationService.getAllStaffs());
			return "reservation_form";
		}
	}

	@PostMapping("/{id}/cancel")
	public String cancelReservation(@PathVariable("id") Long reservationId) {
		reservationService.cancelReservation(reservationId);
		return "redirect:/reservation/history?success=cancelled";
	}

	@GetMapping("/available-slots")
	@ResponseBody
	public List<LocalTime> getAvailableSlots(
			@RequestParam("staffId") Long staffId,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return reservationService.getAvailableTimeSlots(staffId, date);
	}
}
