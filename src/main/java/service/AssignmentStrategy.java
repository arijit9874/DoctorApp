package service;

import model.Doctor;
import model.Patient;
import java.util.List;

public interface AssignmentStrategy {
    /**
     * Assigns a doctor for an appointment based on the strategy implementation
     * @param doctors The list of available doctors
     * @param patient The patient requesting the appointment
     * @return The assigned doctor, or null if no doctor can be assigned
     */
    Doctor assignDoctor(List<Doctor> doctors, Patient patient);
}