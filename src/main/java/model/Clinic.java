package model;

import java.util.*;

public class Clinic {
    public int id;
    public String name;
    public List<Doctor> doctors = new ArrayList<>();

    public Clinic(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
