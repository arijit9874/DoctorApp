package dto;

public class PatientDTO {
    public int id;
    public String name;
    public String phoneNumber;
    public Integer preferredDoctorId;
    public int totalVisits;
    public int noShowCount;
    public double reliabilityScore;
    public String createdAt;

    public PatientDTO() {}

    public PatientDTO(int id, String name, String phoneNumber, Integer preferredDoctorId, int totalVisits, int noShowCount, double reliabilityScore, String createdAt) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.preferredDoctorId = preferredDoctorId;
        this.totalVisits = totalVisits;
        this.noShowCount = noShowCount;
        this.reliabilityScore = reliabilityScore;
        this.createdAt = createdAt;
    }
}