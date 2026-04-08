package api;

import dto.LeadDTO;
import dto.PatientDTO;
import dto.DoctorDTO;
import model.Lead;
import service.ClinicService;
import java.util.List;
import java.util.stream.Collectors;

public class LeadController {
    private final ClinicService clinicService;

    public LeadController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    // GET /leads
    public List<LeadDTO> getLeads() {
        return clinicService.getLeads().stream()
            .map(this::mapToLeadDTO)
            .collect(Collectors.toList());
    }

    private LeadDTO mapToLeadDTO(Lead lead) {
        PatientDTO patientDTO = new PatientDTO(lead.patient.id, lead.patient.name, lead.patient.phoneNumber, lead.patient.preferredDoctorId, lead.patient.totalVisits, lead.patient.noShowCount, lead.patient.reliabilityScore, lead.patient.createdAt.toString());
        DoctorDTO doctorDTO = new DoctorDTO(lead.doctor.id, lead.doctor.name, lead.doctor.specialty, lead.doctor.priority, lead.doctor.clinicId, lead.doctor.isAvailable, lead.doctor.workingStartTime, lead.doctor.workingEndTime);
        return new LeadDTO(lead.id, patientDTO, doctorDTO, lead.appointmentUtc.toString(), lead.durationMinutes, lead.clinicId, lead.state, lead.notes);
    }
}