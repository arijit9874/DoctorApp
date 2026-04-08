package service;

import model.Doctor;
import model.Patient;
import java.util.List;

public class RoundRobinStrategy implements AssignmentStrategy {
    @Override
    public Doctor assignDoctor(List<Doctor> doctors, Patient patient) {
        // Simple round robin: pick the first available doctor
        return doctors.isEmpty() ? null : doctors.get(0);
    }
}