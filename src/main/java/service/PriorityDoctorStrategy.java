package service;

import model.Doctor;
import model.Patient;
import java.util.Comparator;
import java.util.List;

public class PriorityDoctorStrategy implements AssignmentStrategy {

    @Override
    public Doctor assignDoctor(List<Doctor> doctors, Patient patient) {
        // Prioritize doctors by priority level (higher priority first), then by name for tie-breaking
        return doctors.stream()
            .max(Comparator.comparingInt((Doctor doctor) -> doctor.priority)
                        .thenComparing(doctor -> doctor.name))
            .orElse(null);
    }
}