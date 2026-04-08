package service;

import model.Doctor;
import model.Patient;
import java.util.List;

public class LeastBusyStrategy implements AssignmentStrategy {
    @Override
    public Doctor assignDoctor(List<Doctor> doctors, Patient patient) {
        // For now, pick the first doctor (least busy logic would require service access)
        return doctors.isEmpty() ? null : doctors.get(0);
    }
}