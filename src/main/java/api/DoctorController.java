package api;

import dto.DoctorCreateDTO;
import dto.DoctorDTO;
import model.Doctor;
import service.ClinicService;

public class DoctorController {
    private final ClinicService clinicService;

    public DoctorController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    // POST /doctors
    public DoctorDTO createDoctor(DoctorCreateDTO dto) {
        Doctor doctor = clinicService.createDoctor(dto.name, dto.specialty, dto.priority, dto.clinicId);
        return new DoctorDTO(doctor.id, doctor.name, doctor.specialty, doctor.priority, doctor.clinicId, doctor.isAvailable, doctor.workingStartTime, doctor.workingEndTime);
    }
}