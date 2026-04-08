package dto;

import model.BookingState;

public class LeadDTO {
    public int id;
    public PatientDTO patient;
    public DoctorDTO doctor;
    public String appointmentUtc;
    public int durationMinutes;
    public int clinicId;
    public BookingState state;
    public String notes;

    public LeadDTO() {}

    public LeadDTO(int id, PatientDTO patient, DoctorDTO doctor, String appointmentUtc, int durationMinutes, int clinicId, BookingState state, String notes) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentUtc = appointmentUtc;
        this.durationMinutes = durationMinutes;
        this.clinicId = clinicId;
        this.state = state;
        this.notes = notes;
    }
}