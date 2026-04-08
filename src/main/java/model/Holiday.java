package model;

import java.time.LocalDate;

public class Holiday {
    public int id;
    public int doctorId; // -1 for clinic-wide holidays
    public LocalDate date;
    public String reason;

    public Holiday(int id, int doctorId, LocalDate date, String reason) {
        this.id = id;
        this.doctorId = doctorId;
        this.date = date;
        this.reason = reason;
    }

    public boolean isClinicWide() {
        return doctorId == -1;
    }
}