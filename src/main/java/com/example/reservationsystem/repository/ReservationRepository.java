package com.example.reservationsystem.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.reservationsystem.entity.Reservation;
import com.example.reservationsystem.entity.User;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByUserOrderByDateDescTimeSlotDesc(User user);

	List<Reservation> findByStaffOrderByDateDescTimeSlotDesc(User staff);

	Optional<Reservation> findByDateAndTimeSlotAndStaff(LocalDate date, LocalTime timeSlot, User staff);

	List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate);

	List<Reservation> findByStaffAndDateBetween(User staff, LocalDate startDate, LocalDate endDate);
}
