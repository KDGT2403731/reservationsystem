package com.example.reservationsystem.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reservationsystem.entity.Shift;
import com.example.reservationsystem.entity.User;
import com.example.reservationsystem.repository.ShiftRepository;
import com.example.reservationsystem.repository.UserRepository;

@Service
public class ShiftService {
	private final ShiftRepository shiftRepository;
	private final UserRepository userRepository;

	public ShiftService(ShiftRepository shiftRepository, UserRepository userRepository) {
		this.shiftRepository = shiftRepository;
		this.userRepository = userRepository;
	}

	public List<Shift> getStaffShifts(User staff) {
		return shiftRepository.findByStaffOrderByDateAscStartTimeAsc(staff);
	}

	public List<Shift> getShiftsByStaffId(Long staffId) {
		User staff = userRepository.findById(staffId)
				.orElseThrow(() -> new IllegalArgumentException("Staff not found"));
		return shiftRepository.findByStaffOrderByDateAscStartTimeAsc(staff);
	}

	public Optional<Shift> getShiftById(Long id) {
		return shiftRepository.findById(id);
	}

	@Transactional
	public Shift createOrUpdateShift(Long staffId, LocalDate date, LocalTime startTime, LocalTime endTime) {
		User staff = userRepository.findById(staffId)
				.orElseThrow(() -> new IllegalArgumentException("Staff not found"));
		Optional<Shift> existingShift = shiftRepository.findByStaffAndDate(staff, date);
		Shift shift;
		if (existingShift.isPresent()) {
			shift = existingShift.get();
			shift.setStartTime(startTime);
			shift.setEndTime(endTime);
		} else {
			shift = new Shift();
			shift.setStaff(staff);
			shift.setDate(date);
			shift.setStartTime(startTime);
			shift.setEndTime(endTime);
		}
		return shiftRepository.save(shift);
	}

	@Transactional
	public void deleteShift(Long shiftId) {
		shiftRepository.deleteById(shiftId);
	}

	public List<Shift> getAllShifts() {
		return shiftRepository.findAll();
	}

	public List<Shift> getShiftsByDateRange(LocalDate startDate, LocalDate endTime) {
		return shiftRepository.findByDateBetween(startDate, endTime);
	}
}
