package com.example.reservationsystem.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shift")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // シフトID

	// Userエンティティとの多対一の関係 (Many-to-One)
	@ManyToOne
	@JoinColumn(name = "staff_id") // データベースのカラム名
	private User staff; // 担当スタッフ

	@Column(name = "record_date", nullable = false)
	private LocalDate date; // シフト日

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime; // 開始時刻

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime; // 終了時刻
}
