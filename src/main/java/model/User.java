package model;

public class User {
    public int id;
    public String username;
    public Role role;
    public Integer clinicId;

    public User(int id, String username, Role role, Integer clinicId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.clinicId = clinicId;
    }
}
