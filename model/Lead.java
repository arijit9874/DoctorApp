package model;

public class Lead {
    public String patientName;
    public Doctor doctor;
    public String timeSlot;
    public int clinicId;
    public BookingState state;

    public Lead(String patientName, Doctor doctor, String timeSlot, int clinicId) {
        this.patientName = patientName;
        this.doctor = doctor;
        this.timeSlot = timeSlot;
        this.clinicId = clinicId;
        this.state = BookingState.PENDING; // Default to PENDING
    }

    public Lead(String patientName, Doctor doctor, String timeSlot, int clinicId, BookingState state) {
        this.patientName = patientName;
        this.doctor = doctor;
        this.timeSlot = timeSlot;
        this.clinicId = clinicId;
        this.state = state;
    }
}
