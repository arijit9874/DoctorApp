package dto;

import java.time.LocalDate;

public class AppointmentCreateDTO {
    public String patientName;
    public String phoneNumber;
    public int doctorId;
    public LocalDate date;
    public String timeSlot;
    public String zoneId;

    public AppointmentCreateDTO() {}

    public AppointmentCreateDTO(String patientName, String phoneNumber, int doctorId, LocalDate date, String timeSlot, String zoneId) {
        this.patientName = patientName;
        this.phoneNumber = phoneNumber;
        this.doctorId = doctorId;
        this.date = date;
        this.timeSlot = timeSlot;
        this.zoneId = zoneId;
    }
}