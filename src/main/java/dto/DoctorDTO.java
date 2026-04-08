package dto;

public class DoctorDTO {
    public int id;
    public String name;
    public String specialty;
    public int priority;
    public int clinicId;
    public boolean isAvailable;
    public String workingStartTime;
    public String workingEndTime;

    public DoctorDTO() {}

    public DoctorDTO(int id, String name, String specialty, int priority, int clinicId, boolean isAvailable, String workingStartTime, String workingEndTime) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.priority = priority;
        this.clinicId = clinicId;
        this.isAvailable = isAvailable;
        this.workingStartTime = workingStartTime;
        this.workingEndTime = workingEndTime;
    }
}