package service;

import model.Doctor;
import model.Patient;
import java.util.Comparator;
import java.util.List;

public class PriorityStrategy implements AssignmentStrategy {

    @Override
    public Doctor assignDoctor(List<Doctor> doctors, Patient patient) {
        // Prioritize doctors by priority level (higher priority first)
        return doctors.stream()
            .max(Comparator.comparingInt(doctor -> doctor.priority))
            .orElse(null);
    }
}