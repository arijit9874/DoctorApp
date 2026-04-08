package model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Patient {
    public int id;
    public String name;
    public String phoneNumber;
    public Integer preferredDoctorId;
    public String preferredSpecialty;
    public int totalVisits;
    public int noShowCount;
    public double reliabilityScore;
    public Instant createdAt;
    public List<Integer> previousAppointmentIds = new ArrayList<>();

    public Patient(int id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.preferredDoctorId = null;
        this.preferredSpecialty = null;
        this.totalVisits = 0;
        this.noShowCount = 0;
        this.reliabilityScore = 1.0;
        this.createdAt = Instant.now();
    }

    public void setPreferredDoctorId(int doctorId) {
        this.preferredDoctorId = doctorId;
    }

    public void recordAppointment(int appointmentId) {
        previousAppointmentIds.add(appointmentId);
        totalVisits++;
        updateReliability();
    }

    public void recordNoShow() {
        noShowCount++;
        updateReliability();
    }

    public void updateReliability() {
        if (totalVisits > 0) {
            this.reliabilityScore = 1.0 - ((double) noShowCount / totalVisits);
        } else {
            this.reliabilityScore = 1.0;
        }
    }
}
