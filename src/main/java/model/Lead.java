package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Lead {
    public int id;
    public Patient patient;
    public Doctor doctor;
    public Instant appointmentUtc;
    public int durationMinutes;
    public int clinicId;
    public BookingState state;
    public String notes;

    public Lead(int id, Patient patient, Doctor doctor, Instant appointmentUtc, int durationMinutes, int clinicId) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentUtc = appointmentUtc;
        this.durationMinutes = durationMinutes;
        this.clinicId = clinicId;
        this.state = BookingState.PENDING; // Default to PENDING
        this.notes = "";
    }

    public Lead(int id, Patient patient, Doctor doctor, Instant appointmentUtc, int durationMinutes, int clinicId, BookingState state) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentUtc = appointmentUtc;
        this.durationMinutes = durationMinutes;
        this.clinicId = clinicId;
        this.state = state;
        this.notes = "";
    }

    public String getLocalDateTime(ZoneId zone) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(zone)
            .format(appointmentUtc);
    }
}
