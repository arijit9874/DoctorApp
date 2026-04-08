import model.DispatchJob;
import service.DispatchQueue;

public class DispatchTest {
    public static void main(String[] args) {
        DispatchQueue queue = new DispatchQueue();

        // Add some test jobs
        DispatchJob job1 = new DispatchJob(1, "EMAIL_NOTIFICATION", "{\"to\":\"patient@example.com\",\"subject\":\"Appointment Confirmed\"}");
        DispatchJob job2 = new DispatchJob(2, "SMS_REMINDER", "{\"to\":\"+1234567890\",\"message\":\"Appointment tomorrow\"}");
        DispatchJob job3 = new DispatchJob(3, "PUSH_NOTIFICATION", "{\"userId\":\"123\",\"title\":\"Appointment Update\"}");

        queue.enqueueJob(job1);
        queue.enqueueJob(job2);
        queue.enqueueJob(job3);

        // Wait for processing
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Show results
        System.out.println("\n=== Dispatch Job Results ===");
        for (DispatchJob job : queue.getProcessedJobs()) {
            System.out.println("Job " + job.id + " [" + job.jobType + "] - Status: " + job.status +
                             " - Retries: " + job.retryCount +
                             (job.errorMessage != null ? " - Error: " + job.errorMessage : ""));
        }

        queue.shutdown();
    }
}