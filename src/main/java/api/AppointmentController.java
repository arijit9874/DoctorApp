package api;

import dto.AppointmentCreateDTO;
import dto.LeadDTO;
import dto.PatientDTO;
import dto.DoctorDTO;
import model.Lead;
import model.Doctor;
import service.ClinicService;
import java.time.ZoneId;

public class AppointmentController {
    private final ClinicService clinicService;

    public AppointmentController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    // POST /appointments
    public LeadDTO createAppointment(AppointmentCreateDTO dto) {
        Lead lead = clinicService.bookAppointment(dto.patientName, dto.phoneNumber, dto.doctorId, dto.date, dto.timeSlot, ZoneId.of(dto.zoneId));
        return mapToLeadDTO(lead);
    }

    private LeadDTO mapToLeadDTO(Lead lead) {
        PatientDTO patientDTO = new PatientDTO(lead.patient.id, lead.patient.name, lead.patient.phoneNumber, lead.patient.preferredDoctorId, lead.patient.totalVisits, lead.patient.noShowCount, lead.patient.reliabilityScore, lead.patient.createdAt.toString());
        DoctorDTO doctorDTO = new DoctorDTO(lead.doctor.id, lead.doctor.name, lead.doctor.specialty, lead.doctor.priority, lead.doctor.clinicId, lead.doctor.isAvailable, lead.doctor.workingStartTime, lead.doctor.workingEndTime);
        return new LeadDTO(lead.id, patientDTO, doctorDTO, lead.appointmentUtc.toString(), lead.durationMinutes, lead.clinicId, lead.state, lead.notes);
    }
}