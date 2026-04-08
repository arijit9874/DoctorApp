package main;

import java.time.*;
import java.util.List;
import model.*;
import service.ClinicService;

public class Main {

    public static void main(String[] args) {

        ClinicService service = new ClinicService();

        // Initialize dispatch queue
        service.initializeDispatchQueue();

        // === SETUP CLINICS AND DOCTORS ===
        System.out.println("=== Setting up Clinics and Doctors ===");
        Clinic c1 = service.addClinic("Apollo Clinic");
        Clinic c2 = service.addClinic("City Care");

        Doctor d1 = service.addDoctor("Dr. Sharma", c1.id);
        Doctor d2 = service.addDoctor("Dr. Mehta", c1.id);
        Doctor d3 = service.addDoctor("Dr. Roy", c2.id);

        // === CONFIGURE DOCTOR AVAILABILITIES ===
        System.out.println("\n=== Configuring Doctor Availabilities ===");

        // Dr. Sharma: Monday to Friday, 9 AM - 5 PM, 30 min slots
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            DoctorAvailability avail = service.addDoctorAvailability(d1.id, day, LocalTime.of(9, 0), LocalTime.of(17, 0), 30);
            // Add lunch break 12:00 PM - 1:00 PM
            service.addBreakToAvailability(avail.id, LocalTime.of(12, 0), LocalTime.of(13, 0));
        }

        // Dr. Mehta: Tuesday, Thursday, Saturday, 10 AM - 4 PM, 45 min slots
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY}) {
            service.addDoctorAvailability(d2.id, day, LocalTime.of(10, 0), LocalTime.of(16, 0), 45);
        }

        // Dr. Roy: Monday, Wednesday, Friday, 10 AM - 4 PM, 30 min slots
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY}) {
            service.addDoctorAvailability(d3.id, day, LocalTime.of(10, 0), LocalTime.of(16, 0), 30);
        }

        // === CONFIGURE HOLIDAYS AND EMERGENCY BLOCKS ===
        System.out.println("\n=== Configuring Holidays and Emergency Blocks ===");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfter = LocalDate.now().plusDays(2);

        // Clinic holiday for tomorrow
        service.addHoliday(-1, tomorrow, "Clinic Maintenance");

        // Emergency block for Dr. Sharma tomorrow afternoon
        service.addEmergencyBlock(d1.id, tomorrow.atTime(14, 0), tomorrow.atTime(16, 0), "Emergency Meeting");

        // === CONFIGURE AVAILABILITY AND WORKING HOURS (LEGACY) ===
        System.out.println("\n=== Configuring Doctor Availability ===");
        service.setDoctorAvailability(d2.id, false); // Dr. Mehta is unavailable
        service.setDoctorWorkingHours(d3.id, "10:00", "16:00"); // Dr. Roy works 10 AM to 4 PM
        System.out.println("Dr. Mehta set to unavailable");
        System.out.println("Dr. Roy working hours: 10:00 AM - 4:00 PM");

        // === DISPLAY AVAILABLE SLOTS FOR DAY AFTER TOMORROW ===
        System.out.println("\n=== Available Slots for Day After Tomorrow (" + dayAfter + " - " + dayAfter.getDayOfWeek() + ") ===");
        System.out.println("Dr. Sharma available slots:");
        service.getAvailableSlots(d1.id, dayAfter).forEach(slot ->
            System.out.print(slot + " "));
        System.out.println("\nDr. Mehta available slots:");
        service.getAvailableSlots(d2.id, dayAfter).forEach(slot ->
            System.out.print(slot + " "));
        System.out.println("\nDr. Roy available slots:");
        service.getAvailableSlots(d3.id, dayAfter).forEach(slot ->
            System.out.print(slot + " "));
        System.out.println();

        // === SUCCESSFUL BOOKINGS ===
        System.out.println("\n=== Testing Successful Bookings ===");

        // Specific doctor bookings
        System.out.println("1. Specific doctor bookings:");
        service.bookAppointment("Arijit", d1.id, "10:00 AM");
        service.bookAppointment("Rahul", d1.id, "11:00 AM");
        service.bookAppointment("Sneha", d2.id, "12:00 PM"); // This should fail - doctor unavailable

        // Clinic round robin
        System.out.println("\n2. Clinic round robin assignments:");
        service.bookAppointmentRoundRobin("Priya", c1.id, "02:00 PM");
        service.bookAppointmentRoundRobin("Amit", c1.id, "03:00 PM");
        service.bookAppointmentRoundRobin("Riya", c1.id, "04:00 PM");

        // Specific assignment for clinic c2
        System.out.println("\n3. Specific assignment for clinic c2:");
        service.bookAppointment("Vikram", d3.id, "12:00 PM"); // Within Dr. Roy's hours

        // Global auto assignment
        System.out.println("\n4. Global auto assignment:");
        service.bookAppointmentAuto("Global1", "01:00 PM");
        service.bookAppointmentAuto("Global2", "03:30 PM");

        // === VALIDATION TESTS ===
        System.out.println("\n=== Testing Validations ===");

        // Time slot format validation
        System.out.println("1. Invalid time slot formats:");
        service.bookAppointment("Test1", d1.id, "25:00 AM"); // Invalid hour
        service.bookAppointment("Test2", d1.id, "10:60 AM"); // Invalid minute
        service.bookAppointment("Test3", d1.id, "10:00"); // Missing AM/PM

        // Business hours validation
        System.out.println("\n2. Outside business hours:");
        service.bookAppointment("Test4", d1.id, "08:00 AM"); // Too early
        service.bookAppointment("Test5", d1.id, "06:00 PM"); // Too late

        // Doctor availability validation
        System.out.println("\n3. Doctor availability:");
        service.bookAppointment("Test6", d2.id, "01:00 PM"); // Dr. Mehta unavailable

        // Working hours validation
        System.out.println("\n4. Doctor working hours:");
        service.bookAppointment("Test7", d3.id, "09:00 AM"); // Before Dr. Roy's start time
        service.bookAppointment("Test8", d3.id, "05:00 PM"); // After Dr. Roy's end time

        // Double booking prevention
        System.out.println("\n5. Double booking prevention:");
        service.bookAppointment("Test9", d1.id, "10:00 AM"); // Already booked
        service.bookAppointmentRoundRobin("Test10", c1.id, "02:00 PM"); // Already booked in clinic

        // Round robin with no available doctors
        System.out.println("\n6. Round robin edge cases:");
        service.bookAppointmentRoundRobin("Test11", c1.id, "12:00 PM"); // Time when Dr. Mehta would be assigned but unavailable

        // Global auto with conflicts
        System.out.println("\n7. Global auto assignment conflicts:");
        service.bookAppointmentAuto("Test12", "10:00 AM"); // Should find available doctor or fail

        // === DISPLAY RESULTS ===
        System.out.println("\n=== Clinic Isolation Demonstration ===");
        service.showLeadsForClinic(c1.id);
        service.showLeadsForClinic(c2.id);

        // === BOOKING STATE MANAGEMENT ===
        System.out.println("\n=== Testing Booking State Changes ===");

        // Confirm some bookings
        System.out.println("Confirming bookings:");
        service.confirmBooking(d1.id, "10:00 AM", "Arijit"); // Should succeed
        service.confirmBooking(d1.id, "11:00 AM", "Rahul"); // Should succeed
        service.confirmBooking(d3.id, "12:00 PM", "Vikram"); // Should succeed

        // Try to confirm already confirmed
        service.confirmBooking(d1.id, "10:00 AM", "Arijit"); // Should fail

        // Cancel a booking
        System.out.println("\nCancelling booking:");
        service.cancelBooking(d1.id, "02:00 PM", "Priya"); // Should succeed

        // Mark as no-show
        System.out.println("\nMarking as no-show:");
        service.markNoShow(d3.id, "12:00 PM", "Vikram"); // Should succeed

        // Test slot locking with states
        System.out.println("\nTesting slot locking with states:");
        service.bookAppointment("Test13", d1.id, "10:00 AM"); // Should fail - slot locked by CONFIRMED booking
        service.bookAppointment("Test14", d1.id, "02:00 PM"); // Should succeed - slot freed by CANCELLED booking

        service.showLeads();

        // === DISPATCH JOB SYSTEM DEMONSTRATION ===
        System.out.println("\n=== Dispatch Job System Status ===");

        // Wait a bit for async jobs to process
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<DispatchJob> processedJobs = service.getProcessedDispatchJobs();
        System.out.println("Total processed jobs: " + processedJobs.size());

        for (DispatchJob job : processedJobs) {
            System.out.println("Job " + job.id + " [" + job.jobType + "] - Status: " + job.status +
                             (job.status == DispatchJob.DispatchStatus.FAILED ? " (Error: " + job.errorMessage + ")" : ""));
        }

        // Shutdown the dispatch queue
        service.shutdownDispatchQueue();
    }
}
