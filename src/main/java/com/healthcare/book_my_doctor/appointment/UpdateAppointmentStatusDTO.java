package com.healthcare.book_my_doctor.appointment;

import com.healthcare.book_my_doctor.enums.AppointmentStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAppointmentStatusDTO {
	
	@NotNull(message = "Appointment status cannot be null")
    @Enumerated(EnumType.STRING)
	AppointmentStatus appointmentStatus;
	
}
