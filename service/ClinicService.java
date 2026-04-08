package service;

import java.util.*;
import java.util.regex.Pattern;
import model.*;

public class ClinicService {

    private List<Clinic> clinics = new ArrayList<>();
    private List<Doctor> doctors = new ArrayList<>();
    private List<Lead> leads = new ArrayList<>();

    private int clinicIdCounter = 1;
    private int doctorIdCounter = 1;
    private Map<Integer, Integer> lastAssignedDoctorIndex = new HashMap<>();

    public Clinic addClinic(String name) {
        Clinic clinic = new Clinic(clinicIdCounter++, name);
        clinics.add(clinic);
        return clinic;
    }

    public Doctor addDoctor(String name, int clinicId) {
        Doctor doctor = new Doctor(doctorIdCounter++, name, clinicId);
        doctors.add(doctor);

        for (Clinic c : clinics) {
            if (c.id == clinicId) {
                c.doctors.add(doctor);
                break;
            }
        }
        return doctor;
    }

    public void setDoctorAvailability(int doctorId, boolean available) {
        for (Doctor d : doctors) {
            if (d.id == doctorId) {
                d.isAvailable = available;
                break;
            }
        }
    }

    public void setDoctorWorkingHours(int doctorId, String startTime, String endTime) {
        for (Doctor d : doctors) {
            if (d.id == doctorId) {
                d.workingStartTime = startTime;
                d.workingEndTime = endTime;
                break;
            }
        }
    }

    public List<Doctor> getDoctorsForClinic(int clinicId) {
        for (Clinic c : clinics) {
            if (c.id == clinicId) {
                return new ArrayList<>(c.doctors); // Return a copy to prevent external modification
            }
        }
        return new ArrayList<>();
    }

    public List<Lead> getLeadsForClinic(int clinicId) {
        List<Lead> clinicLeads = new ArrayList<>();
        for (Lead l : leads) {
            if (l.clinicId == clinicId) {
                clinicLeads.add(l);
            }
        }
        return clinicLeads;
    }

    public boolean confirmBooking(int doctorId, String timeSlot, String patientName) {
        for (Lead lead : leads) {
            if (lead.doctor.id == doctorId && lead.timeSlot.equals(timeSlot) && lead.patientName.equals(patientName)) {
                if (lead.state == BookingState.PENDING) {
                    lead.state = BookingState.CONFIRMED;
                    return true;
                }
            }
        }
        return false; // Booking not found or not in PENDING state
    }

    public boolean cancelBooking(int doctorId, String timeSlot, String patientName) {
        for (Lead lead : leads) {
            if (lead.doctor.id == doctorId && lead.timeSlot.equals(timeSlot) && lead.patientName.equals(patientName)) {
                if (lead.state == BookingState.PENDING || lead.state == BookingState.CONFIRMED) {
                    lead.state = BookingState.CANCELLED;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean markNoShow(int doctorId, String timeSlot, String patientName) {
        for (Lead lead : leads) {
            if (lead.doctor.id == doctorId && lead.timeSlot.equals(timeSlot) && lead.patientName.equals(patientName)) {
                if (lead.state == BookingState.CONFIRMED) {
                    lead.state = BookingState.NO_SHOW;
                    return true;
                }
            }
        }
        return false;
    }

    public void bookAppointment(String patientName, int doctorId, String timeSlot) {
        Doctor selectedDoctor = null;

        for (Doctor d : doctors) {
            if (d.id == doctorId) {
                selectedDoctor = d;
                break;
            }
        }

        if (selectedDoctor == null) {
            System.out.println("Doctor not found!");
            return;
        }

        if (!selectedDoctor.isAvailable) {
            System.out.println("Doctor is not available!");
            return;
        }

        if (!selectedDoctor.isTimeWithinWorkingHours(timeSlot)) {
            System.out.println("Time slot is outside doctor's working hours!");
            return;
        }

        if (!isValidTimeSlot(timeSlot)) {
            System.out.println("Invalid time slot! Please use HH:MM AM/PM format between 9:00 AM and 5:00 PM.");
            return;
        }

        if (!isTimeSlotAvailable(selectedDoctor, timeSlot)) {
            System.out.println("Time slot already booked for this doctor!");
            return;
        }

        leads.add(new Lead(patientName, selectedDoctor, timeSlot, selectedDoctor.clinicId));
    }

    public void bookAppointmentRoundRobin(String patientName, int clinicId, String timeSlot) {
        Clinic selectedClinic = null;

        for (Clinic c : clinics) {
            if (c.id == clinicId) {
                selectedClinic = c;
                break;
            }
        }

        if (selectedClinic == null) {
            System.out.println("Clinic not found!");
            return;
        }

        if (selectedClinic.doctors.isEmpty()) {
            System.out.println("No doctors available in this clinic!");
            return;
        }

        if (!isValidTimeSlot(timeSlot)) {
            System.out.println("Invalid time slot! Please use HH:MM AM/PM format between 9:00 AM and 5:00 PM.");
            return;
        }

        int lastIndex = lastAssignedDoctorIndex.getOrDefault(clinicId, -1);
        Doctor assignedDoctor = null;
        int checked = 0;
        int currentIndex = lastIndex;
        while (checked < selectedClinic.doctors.size()) {
            currentIndex = (currentIndex + 1) % selectedClinic.doctors.size();
            Doctor candidate = selectedClinic.doctors.get(currentIndex);
            if (candidate.isAvailable && candidate.isTimeWithinWorkingHours(timeSlot)) {
                assignedDoctor = candidate;
                break;
            }
            checked++;
        }

        if (assignedDoctor == null) {
            System.out.println("No available doctor found for this time slot!");
            return;
        }

        lastAssignedDoctorIndex.put(clinicId, selectedClinic.doctors.indexOf(assignedDoctor));

        // Check availability for the assigned doctor
        if (!isTimeSlotAvailable(assignedDoctor, timeSlot)) {
            System.out.println("Time slot already booked for the assigned doctor!");
            return;
        }

        leads.add(new Lead(patientName, assignedDoctor, timeSlot, clinicId));
    }

    public void bookAppointmentAuto(String patientName, String timeSlot) {
        if (!isValidTimeSlot(timeSlot)) {
            System.out.println("Invalid time slot! Please use HH:MM AM/PM format between 9:00 AM and 5:00 PM.");
            return;
        }

        // Global round robin across all doctors
        int lastGlobalIndex = lastAssignedDoctorIndex.getOrDefault(0, -1); // Use 0 as global key
        Doctor assignedDoctor = null;
        int checked = 0;
        int currentIndex = lastGlobalIndex;
        while (checked < doctors.size()) {
            currentIndex = (currentIndex + 1) % doctors.size();
            Doctor candidate = doctors.get(currentIndex);
            if (candidate.isAvailable && candidate.isTimeWithinWorkingHours(timeSlot) && isTimeSlotAvailable(candidate, timeSlot)) {
                assignedDoctor = candidate;
                break;
            }
            checked++;
        }

        if (assignedDoctor == null) {
            System.out.println("No available doctor found for this time slot!");
            return;
        }

        lastAssignedDoctorIndex.put(0, doctors.indexOf(assignedDoctor));

        leads.add(new Lead(patientName, assignedDoctor, timeSlot, assignedDoctor.clinicId));
    }

    private boolean isValidTimeSlot(String timeSlot) {
        // Regex for HH:MM AM/PM format
        Pattern pattern = Pattern.compile("^(0?[1-9]|1[0-2]):[0-5][0-9] (AM|PM)$");
        if (!pattern.matcher(timeSlot).matches()) {
            return false;
        }

        // Parse hour and check if within 9 AM to 5 PM
        String[] parts = timeSlot.split(" ");
        String time = parts[0];
        String ampm = parts[1];
        int hour = Integer.parseInt(time.split(":")[0]);
        if (ampm.equals("PM") && hour != 12) hour += 12;
        if (ampm.equals("AM") && hour == 12) hour = 0;

        return hour >= 9 && hour <= 17; // 9 AM to 5 PM
    }

    private boolean isTimeSlotAvailable(Doctor doctor, String timeSlot) {
        for (Lead lead : leads) {
            if (lead.doctor.id == doctor.id && lead.timeSlot.equals(timeSlot) && lead.state == BookingState.CONFIRMED) {
                return false;
            }
        }
        return true;
    }

    public void showLeadsForClinic(int clinicId) {
        Clinic clinic = null;
        for (Clinic c : clinics) {
            if (c.id == clinicId) {
                clinic = c;
                break;
            }
        }

        if (clinic == null) {
            System.out.println("Clinic not found!");
            return;
        }

        List<Lead> clinicLeads = new ArrayList<>();
        for (Lead l : leads) {
            if (l.clinicId == clinicId) {
                clinicLeads.add(l);
            }
        }

        if (clinicLeads.isEmpty()) {
            System.out.println("No leads found for clinic: " + clinic.name);
            return;
        }

        System.out.println("\n=== LEADS FOR CLINIC: " + clinic.name + " ===");

        Map<Integer, List<Lead>> doctorLeadMap = new HashMap<>();
        for (Lead l : clinicLeads) {
            doctorLeadMap
                .computeIfAbsent(l.doctor.id, k -> new ArrayList<>())
                .add(l);
        }

        for (Doctor d : clinic.doctors) {
            List<Lead> docLeads = doctorLeadMap.get(d.id);
            if (docLeads != null) {
                System.out.println("\nDoctor: " + d.name);
                for (Lead l : docLeads) {
                    System.out.println("- " + l.patientName + " at " + l.timeSlot + " [" + l.state + "]");
                }
            }
        }
    }

    public void showLeads() {
        if (leads.isEmpty()) {
            System.out.println("No leads found!");
            return;
        }

        System.out.println("\n=== NEXT DAY LEADS ===");

        // Group leads by clinic first
        Map<Integer, Map<Integer, List<Lead>>> clinicDoctorLeadMap = new HashMap<>();

        for (Lead l : leads) {
            clinicDoctorLeadMap
                .computeIfAbsent(l.clinicId, k -> new HashMap<>())
                .computeIfAbsent(l.doctor.id, k -> new ArrayList<>())
                .add(l);
        }

        // Display by clinic
        for (Clinic c : clinics) {
            Map<Integer, List<Lead>> doctorLeadMap = clinicDoctorLeadMap.get(c.id);
            if (doctorLeadMap != null && !doctorLeadMap.isEmpty()) {
                System.out.println("\nClinic: " + c.name);
                for (Doctor d : c.doctors) {
                    List<Lead> docLeads = doctorLeadMap.get(d.id);
                    if (docLeads != null) {
                        System.out.println("  Doctor: " + d.name);
                        for (Lead l : docLeads) {
                            System.out.println("- " + l.patientName + " at " + l.timeSlot + " [" + l.state + "]");
                        }
                    }
                }
            }
        }
    }
}
