package com.healthcare.book_my_doctor.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.healthcare.book_my_doctor.enums.AppointmentStatus;
import com.healthcare.book_my_doctor.enums.AppointmentTimeSlot;
import com.healthcare.book_my_doctor.enums.ResponseStatus;
import com.healthcare.book_my_doctor.enums.UserRole;
import com.healthcare.book_my_doctor.envelope.ResponseEnvelope;
import com.healthcare.book_my_doctor.exceptions.AppointmentNotFoundException;
import com.healthcare.book_my_doctor.exceptions.UnauthorizedAccessException;
import com.healthcare.book_my_doctor.exceptions.UserNotFoundException;
import com.healthcare.book_my_doctor.user.UserDTO;
import com.healthcare.book_my_doctor.user.UserRepository;
import com.healthcare.book_my_doctor.user.UserService;

import jakarta.transaction.Transactional;

@Service
public class AppointmentService {

	@Autowired
	private AppointmentRepository appointmentRepository;
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;

	public Page<AppointmentDTO> findAllAppointments(String doctorId, String patientId, AppointmentStatus status,
			LocalDate scheduledDate, AppointmentTimeSlot slot, Pageable pageable) {

		UserDTO currentUser = userService.getCurrentUser();
		userService.checkAdminOrReceptionistAccess(currentUser);

		return appointmentRepository.findByCriteria(doctorId, patientId, status, scheduledDate, slot, pageable);
	}

	@Transactional
	public AppointmentDTO createAppointment(CreationAppointmentDTO creationAppointmentDTO) {
		UserDTO currentUser = userService.getCurrentUser();

		// Check if the current user is a patient
		if (currentUser.getUserRole() == UserRole.PATIENT) {
			// If current user is a patient, they can only book appointments for themselves
			if (!currentUser.getUserUuid().equals(creationAppointmentDTO.getPatientUuid())) {
				throw new UnauthorizedAccessException("You cannot book an appointment for another user.");
			}
		}

		// Validate that the userId in the request exists
		UserDTO doctor = userRepository.findById(creationAppointmentDTO.getDoctorUuid()).orElseThrow(
				() -> new UserNotFoundException("User not found with ID: " + creationAppointmentDTO.getDoctorUuid()));
		UserDTO patient = userRepository.findById(creationAppointmentDTO.getPatientUuid()).orElseThrow(
				() -> new UserNotFoundException("User not found with ID: " + creationAppointmentDTO.getPatientUuid()));

		// Check roles for doctor and patient
		if (!userService.isDoctor(doctor)) {
			throw new UnauthorizedAccessException("The user with ID " + doctor.getUserUuid() + " is not a valid doctor.");
		}
		if (!userService.isPatient(patient)) {
			throw new UnauthorizedAccessException("The user with ID " + patient.getUserUuid() + " is not a valid patient.");
		}

		String newAppointmentUuid = generateUniqueAppointmentUuid();

		// Create the appointment
		AppointmentDTO appointment = AppointmentDTO.builder().appointmentUuid(newAppointmentUuid) // Assuming this
																									// method exists
				.doctorUuid(doctor.getUserUuid()).patientUuid(patient.getUserUuid())
				.appointmentScheduledDate(creationAppointmentDTO.getAppointmentScheduledDate())
				.appointmentSlot(creationAppointmentDTO.getAppointmentSlot())
				.appointmentStatus(AppointmentStatus.SCHEDULED) // Default status
				.dateTimeOfBookingOfAppointment(LocalDateTime.now())
				.dateTimeOfUpdationOfAppointmentStatus(LocalDateTime.now()).build();

		return appointmentRepository.save(appointment);
	}

	public ResponseEnvelope getUserAppointments(String userId, int page, int size) {
		// Create a Pageable instance
		Pageable pageable = PageRequest.of(page, size);

		// Check if the requested user exists
		UserDTO existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

		UserDTO currentUser = userService.getCurrentUser();

		// Check if the current user is authorized to view the requested user's
		// appointments
		boolean isCurrentUser = userService.isCurrentUser(existingUser, currentUser);
		boolean isAdminOrReceptionist = userService.isAdmin(currentUser) || userService.isReceptionist(currentUser);

		if (!isCurrentUser && !isAdminOrReceptionist) {
			throw new UnauthorizedAccessException("You are not authorized to see appointments of this user.");
		}

		// Fetch appointments based on the current user's role
		Page<AppointmentDTO> appointmentsPage;
		if (userService.isDoctor(currentUser)) {
			appointmentsPage = appointmentRepository.findByDoctorUuid(userId, pageable);
		} else {
			// For admins and receptionists, we assume they can view the patient's
			// appointments
			appointmentsPage = appointmentRepository.findByPatientUuid(userId, pageable);
		}

		// Build and return the response envelope
		return ResponseEnvelope.builder().status(ResponseStatus.SUCCESS)
				.message("Appointments retrieved successfully").data(appointmentsPage.getContent())
				.currentPageNo(appointmentsPage.getNumber()).totalNoOfRecords((int) appointmentsPage.getTotalElements())
				.totalNoOfPages(appointmentsPage.getTotalPages()).recordsPerPage(appointmentsPage.getSize())
				.timeStamp(LocalDateTime.now()).build();
	}

	@Transactional
	public ResponseEnvelope updateAppointmentStatus(String appointmentId,
			UpdateAppointmentStatusDTO statusUpdateDTO) {
		// Fetch the appointment by ID
		AppointmentDTO appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + appointmentId));

		// Get the current logged-in user
		UserDTO currentUser = userService.getCurrentUser();

		// Handle the doctor case (doctors can cancel their own scheduled appointments)
		if (userService.isDoctor(currentUser)) {
			// Ensure the doctor is assigned to the appointment
			if (!currentUser.getUserUuid().equals(appointment.getDoctorUuid())) {
				throw new UnauthorizedAccessException(
						"You are not authorized to cancel appointments where you are not the assigned doctor.");
			}

			// Doctors can only cancel scheduled appointments
			if (!appointment.getAppointmentStatus().equals(AppointmentStatus.SCHEDULED)
					|| !statusUpdateDTO.getAppointmentStatus().equals(AppointmentStatus.CANCELLED)) {
				throw new UnauthorizedAccessException("You can only cancel your scheduled appointments.");
			}
		}

		// Handle the patient case (patients can only cancel their own scheduled appointments)
		else if (currentUser.getUserRole() == UserRole.PATIENT) {
			// Ensure the patient is the one who booked the appointment
			if (!currentUser.getUserUuid().equals(appointment.getPatientUuid())) {
				throw new UnauthorizedAccessException("You are not authorized to cancel someone else's appointment.");
			}

			// Patients can only cancel scheduled appointments
			if (!appointment.getAppointmentStatus().equals(AppointmentStatus.SCHEDULED)
					|| !statusUpdateDTO.getAppointmentStatus().equals(AppointmentStatus.CANCELLED)) {
				throw new UnauthorizedAccessException("You can only cancel your own scheduled appointments.");
			}
		}


		// Handle the receptionist case (can update scheduled appointments to any other status)
		else if (userService.isReceptionist(currentUser)) {
			if (!appointment.getAppointmentStatus().equals(AppointmentStatus.SCHEDULED)) {
				throw new UnauthorizedAccessException("Receptionists can only update scheduled appointments.");
			}
		}

		// Handle the admin case (can update to any status without restrictions)
		else if (userService.isAdmin(currentUser)) {
			// Admins can update to any status, so no restrictions apply here
		}

		// If the current user does not have permission
		else {
			throw new UnauthorizedAccessException("You do not have permission to update this appointment.");
		}

		// Update the appointment status and the update timestamp
		appointment.setAppointmentStatus(statusUpdateDTO.getAppointmentStatus());
		appointment.setDateTimeOfUpdationOfAppointmentStatus(LocalDateTime.now());

		// Save the updated appointment in the repository
		appointmentRepository.save(appointment);

		// Build and return the response envelope
		return ResponseEnvelope.builder().status(ResponseStatus.SUCCESS)
				.message("Appointment status updated successfully.").data(appointment).timeStamp(LocalDateTime.now())
				.build();
	}
	
	@Transactional
    public void deleteAppointment(String appointmentId) {
		
        UserDTO currentUser = userService.getCurrentUser();

        // Check if the current user is an admin
        if (!userService.isAdmin(currentUser)) {
            throw new UnauthorizedAccessException("You do not have permission to delete this appointment.");
        }

        // Check if the appointment exists before trying to delete it
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new AppointmentNotFoundException("Appointment not found with ID: " + appointmentId);
        }

        // Delete the appointment
        appointmentRepository.deleteById(appointmentId);
    }

////////////////////////////////////////////////////////////////////////////////////////////
//                                                 helper methods
	private String generateUniqueAppointmentUuid() {
		String newAppointmentUuid;
		do {
			newAppointmentUuid = userService.generateRandomString(16);
		} while (appointmentRepository.existsById(newAppointmentUuid));
		return newAppointmentUuid;
	}
}
