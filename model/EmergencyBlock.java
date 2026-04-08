package model;

import java.time.LocalDateTime;

public class EmergencyBlock {
    public int id;
    public int doctorId; // -1 for clinic-wide blocks
    public LocalDateTime startDateTime;
    public LocalDateTime endDateTime;
    public String reason;

    public EmergencyBlock(int id, int doctorId, LocalDateTime startDateTime,
                         LocalDateTime endDateTime, String reason) {
        this.id = id;
        this.doctorId = doctorId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.reason = reason;
    }

    public boolean isClinicWide() {
        return doctorId == -1;
    }

    public boolean overlapsWith(LocalDateTime checkStart, LocalDateTime checkEnd) {
        return !(endDateTime.isBefore(checkStart) || startDateTime.isAfter(checkEnd));
    }
}