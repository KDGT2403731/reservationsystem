package com.example.reservationsystem.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.reservationsystem.entity.Shift;
import com.example.reservationsystem.entity.User;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
	List<Shift> findByStaffOrderByDateAscStartTimeAsc(User staff);

	Optional<Shift> findByStaffAndDate(User staff, LocalDate date);

	List<Shift> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
