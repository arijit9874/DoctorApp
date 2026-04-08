package service;

import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import model.*;
public class ClinicService {

    private List<Clinic> clinics = new ArrayList<>();
    private List<Doctor> doctors = new ArrayList<>();
    private List<Patient> patients = new ArrayList<>();
    private List<Lead> leads = new ArrayList<>();
    private List<DoctorAvailability> availabilities = new ArrayList<>();
    private List<Holiday> holidays = new ArrayList<>();
    private List<EmergencyBlock> emergencyBlocks = new ArrayList<>();
    private DispatchQueue dispatchQueue;

    private int clinicIdCounter = 1;
    private int doctorIdCounter = 1;
    private int patientIdCounter = 1;
    private int leadIdCounter = 1;
    private int availabilityIdCounter = 1;
    private int holidayIdCounter = 1;
    private int emergencyBlockIdCounter = 1;
    private int dispatchJobIdCounter = 1;
    private Map<Integer, Integer> lastAssignedDoctorIndex = new HashMap<>();

    public void initializeDispatchQueue() {
        this.dispatchQueue = new DispatchQueue();
    }

    public void shutdownDispatchQueue() {
        if (dispatchQueue != null) {
            dispatchQueue.shutdown();
        }
    }

    private void dispatchJob(String jobType, String payload) {
        if (dispatchQueue != null) {
            DispatchJob job = new DispatchJob(dispatchJobIdCounter++, jobType, payload);
            dispatchQueue.enqueueJob(job);
        }
    }

    public List<DispatchJob> getProcessedDispatchJobs() {
        return dispatchQueue != null ? dispatchQueue.getProcessedJobs() : new ArrayList<>();
    }

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

    public Patient findOrCreatePatient(String name, String phoneNumber) {
        for (Patient p : patients) {
            if (p.phoneNumber.equals(phoneNumber)) {
                p.name = name; // keep latest name
                return p;
            }
        }
        Patient patient = new Patient(patientIdCounter++, name, phoneNumber);
        patients.add(patient);
        return patient;
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

    // New availability management methods
    public DoctorAvailability addDoctorAvailability(int doctorId, DayOfWeek dayOfWeek,
                                                  LocalTime startTime, LocalTime endTime,
                                                  int slotDurationMinutes) {
        DoctorAvailability availability = new DoctorAvailability(
            availabilityIdCounter++, doctorId, dayOfWeek, startTime, endTime, slotDurationMinutes);
        availabilities.add(availability);
        return availability;
    }

    public void addBreakToAvailability(int availabilityId, LocalTime breakStart, LocalTime breakEnd) {
        for (DoctorAvailability avail : availabilities) {
            if (avail.id == availabilityId) {
                avail.addBreak(breakStart, breakEnd);
                break;
            }
        }
    }

    private Instant toUtcInstant(LocalDate date, String timeSlot, ZoneId zone) {
        try {
            String[] parts = timeSlot.split(" ");
            if (parts.length != 2) return null;
            String time = parts[0];
            String ampm = parts[1];
            String[] hm = time.split(":");
            if (hm.length != 2) return null;
            int hour = Integer.parseInt(hm[0]);
            int minute = Integer.parseInt(hm[1]);
            if (ampm.equals("PM") && hour != 12) hour += 12;
            if (ampm.equals("AM") && hour == 12) hour = 0;
            LocalTime localTime = LocalTime.of(hour, minute);
            return date.atTime(localTime).atZone(zone).toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    private String toTimeSlot(Instant instant) {
        LocalTime time = instant.atZone(ZoneId.systemDefault()).toLocalTime();
        int hour = time.getHour();
        int minute = time.getMinute();
        String ampm = hour >= 12 ? "PM" : "AM";
        if (hour > 12) hour -= 12;
        if (hour == 0) hour = 12;
        return String.format("%d:%02d %s", hour, minute, ampm);
    }

    public Holiday addHoliday(int doctorId, LocalDate date, String reason) {
        Holiday holiday = new Holiday(holidayIdCounter++, doctorId, date, reason);
        holidays.add(holiday);
        return holiday;
    }

    public EmergencyBlock addEmergencyBlock(int doctorId, LocalDateTime startDateTime,
                                          LocalDateTime endDateTime, String reason) {
        EmergencyBlock block = new EmergencyBlock(emergencyBlockIdCounter++, doctorId,
                                                startDateTime, endDateTime, reason);
        emergencyBlocks.add(block);
        return block;
    }

    public List<LocalTime> getAvailableSlots(int doctorId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Check if it's a holiday
        boolean isHoliday = holidays.stream()
            .anyMatch(h -> (h.doctorId == doctorId || h.doctorId == -1) && h.date.equals(date));

        if (isHoliday) {
            return new ArrayList<>();
        }

        // Find availability for this day
        DoctorAvailability availability = availabilities.stream()
            .filter(a -> a.doctorId == doctorId && a.dayOfWeek == dayOfWeek)
            .findFirst()
            .orElse(null);

        if (availability == null) {
            return new ArrayList<>();
        }

        List<LocalTime> slots = availability.generateAvailableSlots();

        // Filter out slots that overlap with emergency blocks
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime dateEnd = date.atTime(LocalTime.MAX);

        List<EmergencyBlock> relevantBlocks = emergencyBlocks.stream()
            .filter(b -> (b.doctorId == doctorId || b.doctorId == -1) &&
                        b.overlapsWith(dateStart, dateEnd))
            .toList();

        return slots.stream()
            .filter(slot -> {
                LocalDateTime slotDateTime = date.atTime(slot);
                return relevantBlocks.stream()
                    .noneMatch(block -> block.overlapsWith(slotDateTime, slotDateTime.plusMinutes(availability.slotDurationMinutes)));
            })
            .toList();
    }

    public List<Doctor> getDoctorsForClinic(int clinicId) {
        for (Clinic c : clinics) {
            if (c.id == clinicId) {
                return new ArrayList<>(c.doctors); // Return a copy to prevent external modification
            }
        }
        return new ArrayList<>();
    }

    public int getDoctorAppointmentCount(Doctor doctor, LocalDate date) {
        return (int) leads.stream()
            .filter(lead -> lead.doctor.id == doctor.id && lead.state != BookingState.CANCELLED &&
                           lead.appointmentUtc.atZone(ZoneId.systemDefault()).toLocalDate().equals(date))
            .count();
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
        LocalDate bookingDate = LocalDate.now().plusDays(2);
        for (Lead lead : leads) {
            if (lead.doctor.id == doctorId && lead.patient.name.equals(patientName) &&
                lead.appointmentUtc.atZone(ZoneId.systemDefault()).toLocalDate().equals(bookingDate) &&
                toTimeSlot(lead.appointmentUtc).equals(timeSlot)) {
                if (lead.state == BookingState.PENDING) {
                    lead.state = BookingState.CONFIRMED;

                    // Dispatch confirmation job
                    String payload = String.format("{\"patient\":\"%s\",\"doctor\":\"%s\",\"date\":\"%s\",\"time\":\"%s\"}",
                        patientName, lead.doctor.name, bookingDate, timeSlot);
                    dispatchJob("APPOINTMENT_CONFIRMED", payload);

                    return true;
                }
            }
        }
        return false; // Booking not found or not in PENDING state
    }

    public boolean cancelBooking(int doctorId, String timeSlot, String patientName) {
        LocalDate bookingDate = LocalDate.now().plusDays(2);
        for (Lead lead : leads) {
            if (lead.doctor.id == doctorId && lead.patient.name.equals(patientName) &&
                lead.appointmentUtc.atZone(ZoneId.systemDefault()).toLocalDate().equals(bookingDate) &&
                toTimeSlot(lead.appointmentUtc).equals(timeSlot)) {
                if (lead.state == BookingState.PENDING || lead.state == BookingState.CONFIRMED) {
                    lead.state = BookingState.CANCELLED;

                    // Dispatch cancellation job
                    String payload = String.format("{\"patient\":\"%s\",\"doctor\":\"%s\",\"date\":\"%s\",\"time\":\"%s\"}",
                        patientName, lead.doctor.name, bookingDate, timeSlot);
                    dispatchJob("APPOINTMENT_CANCELLED", payload);

                    return true;
                }
            }
        }
        return false;
    }

    public boolean markNoShow(int doctorId, String timeSlot, String patientName) {
        LocalDate bookingDate = LocalDate.now().plusDays(2);
        for (Lead lead : leads) {
            if (lead.doctor.id == doctorId && lead.patient.name.equals(patientName) &&
                lead.appointmentUtc.atZone(ZoneId.systemDefault()).toLocalDate().equals(bookingDate) &&
                toTimeSlot(lead.appointmentUtc).equals(timeSlot)) {
                if (lead.state == BookingState.CONFIRMED) {
                    lead.state = BookingState.NO_SHOW;

                    // Dispatch no-show notification job
                    String payload = String.format("{\"patient\":\"%s\",\"doctor\":\"%s\",\"date\":\"%s\",\"time\":\"%s\"}",
                        patientName, lead.doctor.name, bookingDate, timeSlot);
                    dispatchJob("APPOINTMENT_NO_SHOW", payload);

                    return true;
                }
            }
        }
        return false;
    }

    public void bookAppointment(String patientName, int doctorId, String timeSlot) {
        System.out.println("DEBUG: Booking attempt for doctorId=" + doctorId + ", timeSlot=" + timeSlot);
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

        System.out.println("DEBUG: Booking for doctor " + selectedDoctor.name + " (id=" + selectedDoctor.id + "), available=" + selectedDoctor.isAvailable);

        if (!selectedDoctor.isAvailable) {
            System.out.println("Doctor is not available!");
            return;
        }

        LocalDate bookingDate = LocalDate.now().plusDays(2); // Assume day after tomorrow booking

        List<LocalTime> availableSlots = getAvailableSlots(selectedDoctor.id, bookingDate);
        System.out.println("DEBUG: Available slots for doctor " + selectedDoctor.id + " on " + bookingDate + ": " + availableSlots);

        if (!isTimeSlotAvailable(selectedDoctor, bookingDate, timeSlot)) {
            System.out.println("Time slot already booked or not available for this doctor!");
            return;
        }

        if (!isValidTimeSlot(timeSlot)) {
            System.out.println("Invalid time slot! Please use HH:MM AM/PM format between 9:00 AM and 5:00 PM.");
            return;
        }

        Patient patient = findOrCreatePatient(patientName, "unknown");
        Instant appointmentUtc = toUtcInstant(bookingDate, timeSlot, ZoneId.systemDefault());
        Lead lead = new Lead(leadIdCounter++, patient, selectedDoctor, appointmentUtc, 30, selectedDoctor.clinicId);
        leads.add(lead);
        patient.recordAppointment(lead.id);

        // Dispatch notification job
        String payload = String.format("{\"patient\":\"%s\",\"doctor\":\"%s\",\"appointmentUtc\":\"%s\"}",
            patientName, selectedDoctor.name, appointmentUtc);
        dispatchJob("APPOINTMENT_BOOKED", payload);
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

        LocalDate bookingDate = LocalDate.now().plusDays(2); // Assume day after tomorrow booking

        int lastIndex = lastAssignedDoctorIndex.getOrDefault(clinicId, -1);
        Doctor assignedDoctor = null;
        int checked = 0;
        int currentIndex = lastIndex;
        while (checked < selectedClinic.doctors.size()) {
            currentIndex = (currentIndex + 1) % selectedClinic.doctors.size();
            Doctor candidate = selectedClinic.doctors.get(currentIndex);
            if (candidate.isAvailable && isTimeSlotAvailable(candidate, bookingDate, timeSlot)) {
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

        Patient patient = findOrCreatePatient(patientName, "unknown");
        Instant appointmentUtc = toUtcInstant(bookingDate, timeSlot, ZoneId.systemDefault());
        Lead lead = new Lead(leadIdCounter++, patient, assignedDoctor, appointmentUtc, 30, clinicId);
        leads.add(lead);
        patient.recordAppointment(lead.id);

        // Dispatch notification job
        String payload = String.format("{\"patient\":\"%s\",\"doctor\":\"%s\",\"appointmentUtc\":\"%s\",\"clinic\":\"%s\"}",
            patientName, assignedDoctor.name, appointmentUtc, selectedClinic.name);
        dispatchJob("APPOINTMENT_BOOKED", payload);
    }

    public void bookAppointmentAuto(String patientName, String timeSlot) {
        if (!isValidTimeSlot(timeSlot)) {
            System.out.println("Invalid time slot! Please use HH:MM AM/PM format between 9:00 AM and 5:00 PM.");
            return;
        }

        LocalDate bookingDate = LocalDate.now().plusDays(2); // Assume day after tomorrow booking

        // Global round robin across all doctors
        int lastGlobalIndex = lastAssignedDoctorIndex.getOrDefault(0, -1); // Use 0 as global key
        Doctor assignedDoctor = null;
        int checked = 0;
        int currentIndex = lastGlobalIndex;
        while (checked < doctors.size()) {
            currentIndex = (currentIndex + 1) % doctors.size();
            Doctor candidate = doctors.get(currentIndex);
            if (candidate.isAvailable && isTimeSlotAvailable(candidate, bookingDate, timeSlot)) {
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

        Patient patient = findOrCreatePatient(patientName, "unknown");
        Instant appointmentUtc = toUtcInstant(bookingDate, timeSlot, ZoneId.systemDefault());
        Lead lead = new Lead(leadIdCounter++, patient, assignedDoctor, appointmentUtc, 30, assignedDoctor.clinicId);
        leads.add(lead);
        patient.recordAppointment(lead.id);

        // Dispatch notification job
        String payload = String.format("{\"patient\":\"%s\",\"doctor\":\"%s\",\"appointmentUtc\":\"%s\"}",
            patientName, assignedDoctor.name, appointmentUtc);
        dispatchJob("APPOINTMENT_BOOKED", payload);
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

    private boolean isTimeSlotAvailable(Doctor doctor, LocalDate date, String timeSlot) {
        // Check if slot is already booked
        for (Lead lead : leads) {
            if (lead.doctor.id == doctor.id &&
                lead.appointmentUtc.atZone(ZoneId.systemDefault()).toLocalDate().equals(date) &&
                toTimeSlot(lead.appointmentUtc).equals(timeSlot) &&
                lead.state == BookingState.CONFIRMED) {
                return false;
            }
        }

        // Check if slot is within doctor's available slots for the date
        List<LocalTime> availableSlots = getAvailableSlots(doctor.id, date);
        LocalTime slotTime = parseTimeSlot(timeSlot);
        return availableSlots.contains(slotTime);
    }

    private LocalTime parseTimeSlot(String timeSlot) {
        try {
            // Parse "HH:MM AM/PM" to LocalTime
            String[] parts = timeSlot.split(" ");
            if (parts.length != 2) return null;
            String time = parts[0];
            String ampm = parts[1];
            String[] hm = time.split(":");
            if (hm.length != 2) return null;
            int hour = Integer.parseInt(hm[0]);
            int minute = Integer.parseInt(hm[1]);
            if (ampm.equals("PM") && hour != 12) hour += 12;
            if (ampm.equals("AM") && hour == 12) hour = 0;
            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            return null; // Invalid time slot
        }
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
                    System.out.println("- " + l.patient.name + " on " + l.appointmentUtc.atZone(ZoneId.systemDefault()).toLocalDate() + " at " + toTimeSlot(l.appointmentUtc) + " [" + l.state + "]");
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
                            System.out.println("- " + l.patient.name + " on " + l.appointmentUtc.atZone(ZoneId.systemDefault()).toLocalDate() + " at " + toTimeSlot(l.appointmentUtc) + " [" + l.state + "]");
                        }
                    }
                }
            }
        }
    }

    public Lead bookAppointment(String patientName, String phoneNumber, int doctorId, LocalDate date, String timeSlot, ZoneId zone) {
        if (!isValidTimeSlot(timeSlot)) {
            throw new IllegalArgumentException("Invalid time slot! Please use HH:MM AM/PM format between 9:00 AM and 5:00 PM.");
        }

        Patient patient = findOrCreatePatient(patientName, phoneNumber);
        Doctor doctor = doctors.stream().filter(d -> d.id == doctorId).findFirst().orElse(null);
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor not found!");
        }
        if (!doctor.isAvailable) {
            throw new IllegalArgumentException("Doctor is not available!");
        }
        if (!isTimeSlotAvailable(doctor, date, timeSlot)) {
            throw new IllegalArgumentException("Time slot not available!");
        }

        Instant appointmentUtc = toUtcInstant(date, timeSlot, zone);
        Lead lead = new Lead(leadIdCounter++, patient, doctor, appointmentUtc, 30, doctor.clinicId);
        leads.add(lead);
        patient.recordAppointment(lead.id);

        // Dispatch notification job
        String payload = String.format("{\"patient\":\"%s\",\"doctor\":\"%s\",\"appointmentUtc\":\"%s\"}",
            patientName, doctor.name, appointmentUtc);
        dispatchJob("APPOINTMENT_BOOKED", payload);

        return lead;
    }

    // API support methods
    public Clinic createClinic(String name) {
        return addClinic(name);
    }

    public Doctor createDoctor(String name, String specialty, int priority, int clinicId) {
        Doctor doctor = new Doctor(doctorIdCounter++, name, clinicId);
        doctor.specialty = specialty;
        doctor.priority = priority;
        doctors.add(doctor);

        for (Clinic c : clinics) {
            if (c.id == clinicId) {
                c.doctors.add(doctor);
                break;
            }
        }
        return doctor;
    }

    public List<Lead> getLeads() {
        return new ArrayList<>(leads);
    }
}
