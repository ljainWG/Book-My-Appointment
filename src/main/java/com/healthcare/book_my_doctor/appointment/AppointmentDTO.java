package com.healthcare.book_my_doctor.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.healthcare.book_my_doctor.enums.AppointmentStatus;
import com.healthcare.book_my_doctor.enums.AppointmentTimeSlot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@Entity
@Table(name = "Appointments") // Specifies the table name in the database
public class AppointmentDTO {

	@Id
    @Column(name = "appointment_uuid", nullable = false, length = 255, unique = true)
	String appointmentUuid;
	
	@NotBlank(message = "Doctor UUID cannot be blank")
    @Column(name = "doctor_uuid", nullable = false, length = 255)
	String doctorUuid;
    
	@NotBlank(message = "Patient UUID cannot be blank")
    @Column(name = "patient_uuid", nullable = false, length = 255)
	String patientUuid;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_status", nullable = false, length = 50)
	AppointmentStatus appointmentStatus;
    
    @Column(name = "appointment_scheduled_date", nullable = false)
	LocalDate appointmentScheduledDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_slot", nullable = false, length = 50)
	AppointmentTimeSlot appointmentSlot;
    
    @Column(name = "date_time_of_booking")
	LocalDateTime dateTimeOfBookingOfAppointment;
    
    @Column(name = "date_time_of_updation")
	LocalDateTime dateTimeOfUpdationOfAppointmentStatus;
	
}
