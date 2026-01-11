package com.example.reservationsystem.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.entity.Waitlist;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
	List<Waitlist> findByWaitDate(LocalDate waitDate);

	List<Waitlist> findByUserAndRequestStatus(User user, String requestStatus);

	List<Waitlist> findByWaitDateAndStartTimeAndStaffAndRequestStatus(
			LocalDate waitDate, LocalTime startTime, User staff, String requestStatus);
}
