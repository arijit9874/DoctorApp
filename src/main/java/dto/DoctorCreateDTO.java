package dto;

public class DoctorCreateDTO {
    public String name;
    public String specialty;
    public int priority;
    public int clinicId;

    public DoctorCreateDTO() {}

    public DoctorCreateDTO(String name, String specialty, int priority, int clinicId) {
        this.name = name;
        this.specialty = specialty;
        this.priority = priority;
        this.clinicId = clinicId;
    }
}