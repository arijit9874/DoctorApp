package model;

public class Doctor {
    public int id;
    public String name;
    public String specialty; // e.g., "Cardiology", "Dermatology", "General"
    public int priority; // Higher number = higher priority (VIP doctors)
    public int clinicId;
    public boolean isAvailable;
    public String workingStartTime; // e.g., "09:00"
    public String workingEndTime;   // e.g., "17:00"

    public Doctor(int id, String name, int clinicId) {
        this.id = id;
        this.name = name;
        this.specialty = "General"; // default specialty
        this.priority = 1; // default priority
        this.clinicId = clinicId;
        this.isAvailable = true; // default available
        this.workingStartTime = "09:00"; // default 9 AM
        this.workingEndTime = "17:00";   // default 5 PM
    }

    public Doctor(int id, String name, String specialty, int clinicId) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.priority = 1; // default priority
        this.clinicId = clinicId;
        this.isAvailable = true;
        this.workingStartTime = "09:00";
        this.workingEndTime = "17:00";
    }

    public Doctor(int id, String name, String specialty, int priority, int clinicId) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.priority = priority;
        this.clinicId = clinicId;
        this.isAvailable = true;
        this.workingStartTime = "09:00";
        this.workingEndTime = "17:00";
    }

    public boolean isTimeWithinWorkingHours(String timeSlot) {
        // Extract hour and minute from timeSlot, assuming format "HH:MM AM/PM"
        String[] parts = timeSlot.split(" ");
        if (parts.length != 2) return false;
        String time = parts[0];
        String ampm = parts[1];
        String[] hm = time.split(":");
        if (hm.length != 2) return false;
        int hour = Integer.parseInt(hm[0]);
        int minute = Integer.parseInt(hm[1]);
        if (ampm.equals("PM") && hour != 12) hour += 12;
        if (ampm.equals("AM") && hour == 12) hour = 0;

        // Convert to minutes since midnight
        int slotMinutes = hour * 60 + minute;

        // Parse working hours
        String[] startParts = workingStartTime.split(":");
        int startHour = Integer.parseInt(startParts[0]);
        int startMinute = Integer.parseInt(startParts[1]);
        int startMinutes = startHour * 60 + startMinute;

        String[] endParts = workingEndTime.split(":");
        int endHour = Integer.parseInt(endParts[0]);
        int endMinute = Integer.parseInt(endParts[1]);
        int endMinutes = endHour * 60 + endMinute;

        return slotMinutes >= startMinutes && slotMinutes < endMinutes;
    }
}
