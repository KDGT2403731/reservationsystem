package com.example.reservationsystem.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "waitlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Waitlist {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "staff_id")
	private User staff;

	@Column(name = "wait_date", nullable = false)
	private LocalDate waitDate;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "reservation_menu")
	private String reservationMenu; // メニューID (FK) ではなくStringで仮定

	@Column(name = "request_status", nullable = false)
	private String requestStatus; // 例: PENDING, NOTIFIED, EXPIRED

	@Column(name = "requested_at", nullable = false)
	private LocalDateTime requestedAt;

	@Column(name = "notified_at")
	private LocalDateTime notifiedAt;

	@Column(name = "expiration_time")
	private LocalDateTime expirationTime;

	@Transient
	private String timeRemaining;

	@Transient
	private boolean isExpiringSoon;

	// ゲッター・セッター

	public String getTimeRemaining() {
		return timeRemaining;
	}

	public void setTimeRemaining(String timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	public boolean isExpiringSoon() {
		return isExpiringSoon;
	}

	public void setExpiringSoon(boolean expiringSoon) {
		isExpiringSoon = expiringSoon;
	}
}
