package service;

import model.Doctor;
import model.Patient;
import java.util.List;

public class SpecialtyBasedStrategy implements AssignmentStrategy {
    @Override
    public Doctor assignDoctor(List<Doctor> doctors, Patient patient) {
        // Prioritize doctors with matching specialty
        for (Doctor doctor : doctors) {
            if (doctor.specialty != null && doctor.specialty.equals(patient.preferredSpecialty)) {
                return doctor;
            }
        }
        // Fallback to first doctor if no match
        return doctors.isEmpty() ? null : doctors.get(0);
    }
}