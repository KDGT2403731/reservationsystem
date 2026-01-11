package com.example.reservationsystem.controller;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.repository.ReservationRepository;
import com.example.reservationsystem.repository.UserRepository;

@Controller
public class DashboardController {
	private final UserRepository userRepository;
	private final ReservationRepository reservationRepository;

	public DashboardController(UserRepository userRepository, ReservationRepository reservationRepository) {
		this.userRepository = userRepository;
		this.reservationRepository = reservationRepository;
	}

	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		User currentUser = userRepository.findByEmail(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		if (currentUser.getRole().equals("ADMIN")) {
			model.addAttribute("recentReservations",
					reservationRepository.findByDateBetween(LocalDate.now().minusDays(7), LocalDate.now().plusDays(7)));
			return "admin_dashboard";
		} else if (currentUser.getRole().equals("STAFF")) {
			model.addAttribute("todayReservations",
					reservationRepository.findByStaffAndDateBetween(currentUser, LocalDate.now(), LocalDate.now()));
			return "staff_dashboard";
		} else {
			model.addAttribute("userReservations",
					reservationRepository.findByUserOrderByDateDescTimeSlotDesc(currentUser));
			return "customer_dashboard";
		}
	}
}
