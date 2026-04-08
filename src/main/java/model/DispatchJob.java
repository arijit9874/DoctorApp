package model;

import java.time.LocalDateTime;

public class DispatchJob {
    public int id;
    public String jobType; // e.g., "APPOINTMENT_CONFIRMATION", "REMINDER", "CANCELLATION"
    public String payload; // JSON or serialized data
    public DispatchStatus status;
    public LocalDateTime createdAt;
    public LocalDateTime processedAt;
    public int retryCount;
    public String errorMessage;

    public DispatchJob(int id, String jobType, String payload) {
        this.id = id;
        this.jobType = jobType;
        this.payload = payload;
        this.status = DispatchStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
    }

    public enum DispatchStatus {
        PENDING,
        SENT,
        FAILED
    }
}