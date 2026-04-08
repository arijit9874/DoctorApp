package service;

import java.util.Map;

/**
 * Service for handling monetization and billing calculations
 */
public class BillingService {

    // Pricing constants
    private static final double SUBSCRIPTION_BASE_FEE = 100.0; // Monthly base fee per clinic
    private static final double PER_LEAD_FEE = 5.0; // Fee per lead
    private static final double PER_APPOINTMENT_FEE = 10.0; // Fee per confirmed appointment

    private final ClinicService clinicService;

    public BillingService(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Calculate monthly subscription bill for a clinic
     * Includes base fee + per-lead pricing + per-appointment pricing
     */
    public double calculateMonthlyBill(int clinicId, int year, int month) {
        Map<String, Integer> usage = clinicService.getMonthlyUsageStats(clinicId, year, month);

        int leads = usage.get("leads");
        int appointments = usage.get("appointments");

        double baseFee = SUBSCRIPTION_BASE_FEE;
        double leadFee = leads * PER_LEAD_FEE;
        double appointmentFee = appointments * PER_APPOINTMENT_FEE;

        return baseFee + leadFee + appointmentFee;
    }

    /**
     * Get billing breakdown for a clinic
     */
    public Map<String, Double> getBillingBreakdown(int clinicId, int year, int month) {
        Map<String, Integer> usage = clinicService.getMonthlyUsageStats(clinicId, year, month);

        int leads = usage.get("leads");
        int appointments = usage.get("appointments");

        Map<String, Double> breakdown = new java.util.HashMap<>();
        breakdown.put("baseFee", SUBSCRIPTION_BASE_FEE);
        breakdown.put("leadFee", leads * PER_LEAD_FEE);
        breakdown.put("appointmentFee", appointments * PER_APPOINTMENT_FEE);
        breakdown.put("total", calculateMonthlyBill(clinicId, year, month));

        return breakdown;
    }

    /**
     * Check if clinic is within free tier limits (example: 10 leads, 5 appointments free)
     */
    public boolean isWithinFreeTier(int clinicId) {
        int currentLeads = clinicService.getLeadCountForClinic(clinicId);
        int currentAppointments = clinicService.getAppointmentCountForClinic(clinicId);

        // For simplicity, check total leads and appointments across clinic
        return currentLeads <= 10 && currentAppointments <= 5;
    }

    /**
     * Get usage summary for dashboard
     */
    public Map<String, Object> getUsageSummary(int clinicId) {
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("totalLeads", clinicService.getLeadCountForClinic(clinicId));
        summary.put("totalAppointments", clinicService.getAppointmentCountForClinic(clinicId));
        summary.put("withinFreeTier", isWithinFreeTier(clinicId));

        // Current month usage
        int currentYear = java.time.LocalDate.now().getYear();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        Map<String, Integer> monthlyStats = clinicService.getMonthlyUsageStats(clinicId, currentYear, currentMonth);
        summary.put("currentMonthLeads", monthlyStats.get("leads"));
        summary.put("currentMonthAppointments", monthlyStats.get("appointments"));

        return summary;
    }
}