package api;

import dto.ClinicCreateDTO;
import dto.ClinicDTO;
import model.Clinic;
import service.ClinicService;

public class ClinicController {
    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    // POST /clinics
    public ClinicDTO createClinic(ClinicCreateDTO dto) {
        Clinic clinic = clinicService.createClinic(dto.name);
        return new ClinicDTO(clinic.id, clinic.name);
    }
}