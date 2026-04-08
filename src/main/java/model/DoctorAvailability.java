package model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorAvailability {
    public int id;
    public int doctorId;
    public DayOfWeek dayOfWeek;
    public LocalTime startTime;
    public LocalTime endTime;
    public int slotDurationMinutes;
    public List<BreakTime> breaks;

    public DoctorAvailability(int id, int doctorId, DayOfWeek dayOfWeek,
                            LocalTime startTime, LocalTime endTime, int slotDurationMinutes) {
        this.id = id;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.breaks = new ArrayList<>();
    }

    public void addBreak(LocalTime breakStart, LocalTime breakEnd) {
        breaks.add(new BreakTime(breakStart, breakEnd));
    }

    public List<LocalTime> generateAvailableSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = startTime;

        while (current.isBefore(endTime)) {
            final LocalTime checkTime = current; // Make effectively final for lambda
            // Check if current time falls within any break
            boolean isInBreak = breaks.stream()
                .anyMatch(breakTime -> !checkTime.isBefore(breakTime.start) && checkTime.isBefore(breakTime.end));

            if (!isInBreak) {
                slots.add(current);
            }

            current = current.plusMinutes(slotDurationMinutes);
        }

        return slots;
    }

    public static class BreakTime {
        public LocalTime start;
        public LocalTime end;

        public BreakTime(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }
}