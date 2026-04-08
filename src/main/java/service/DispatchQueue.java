package service;

import java.util.*;
import java.util.concurrent.*;
import model.DispatchJob;

public class DispatchQueue {
    private final BlockingQueue<DispatchJob> jobQueue;
    private final ExecutorService executorService;
    private final List<DispatchJob> processedJobs;
    private final int maxRetries = 3;

    public DispatchQueue() {
        this.jobQueue = new LinkedBlockingQueue<>();
        this.processedJobs = new ArrayList<>();
        this.executorService = Executors.newFixedThreadPool(2); // 2 worker threads

        // Start the worker threads
        startWorkers();
    }

    private void startWorkers() {
        for (int i = 0; i < 2; i++) {
            executorService.submit(this::processJobs);
        }
    }

    private void processJobs() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DispatchJob job = jobQueue.take(); // Blocks until a job is available

                synchronized (processedJobs) {
                    processedJobs.add(job);
                }

                processJob(job);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processJob(DispatchJob job) {
        try {
            // Simulate processing (in real system, this would send email/SMS/etc.)
            System.out.println("Processing job: " + job.jobType + " with payload: " + job.payload);

            // Simulate some processing time
            Thread.sleep(1000);

            // Simulate random failure for demonstration
            if (Math.random() < 0.2 && job.retryCount < maxRetries) { // 20% failure rate
                throw new RuntimeException("Simulated dispatch failure");
            }

            // Success
            job.status = DispatchJob.DispatchStatus.SENT;
            job.processedAt = java.time.LocalDateTime.now();
            System.out.println("Job " + job.id + " completed successfully");

        } catch (Exception e) {
            job.retryCount++;
            job.errorMessage = e.getMessage();

            if (job.retryCount < maxRetries) {
                // Re-queue for retry
                job.status = DispatchJob.DispatchStatus.PENDING;
                jobQueue.offer(job);
                System.out.println("Job " + job.id + " failed, retrying (attempt " + job.retryCount + ")");
            } else {
                // Max retries reached
                job.status = DispatchJob.DispatchStatus.FAILED;
                job.processedAt = java.time.LocalDateTime.now();
                System.out.println("Job " + job.id + " failed permanently after " + maxRetries + " attempts");
            }
        }
    }

    public void enqueueJob(DispatchJob job) {
        jobQueue.offer(job);
        System.out.println("Job " + job.id + " enqueued: " + job.jobType);
    }

    public List<DispatchJob> getProcessedJobs() {
        synchronized (processedJobs) {
            return new ArrayList<>(processedJobs);
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}