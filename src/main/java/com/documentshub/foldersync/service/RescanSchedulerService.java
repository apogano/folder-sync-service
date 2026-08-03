package com.documentshub.foldersync.service;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

/**
 * Runs the scan-then-upload cycle on a repeating interval that can change
 * at runtime (from the settings UI), without restarting the app.
 *
 * Spring's declarative @Scheduled(fixedRate = ...) is evaluated once at
 * startup from a property placeholder and can't be changed afterward --
 * so instead this schedules itself imperatively via
 * ThreadPoolTaskScheduler, keeping a handle to the current ScheduledFuture
 * so it can be cancelled and replaced whenever the interval changes.
 */
@Service
public class RescanSchedulerService{
	private static final Logger log = LoggerFactory.getLogger(RescanSchedulerService.class);
	
	private final ThreadPoolTaskScheduler taskScheduler;
	private final FolderScannerService folderScannerService;
	private final UploadService uploadService;
	private final SettingsService settingsService;
	
	private ScheduledFuture<?> currentSchedule;
	
	public RescanSchedulerService(
			FolderScannerService folderScannerService,
			UploadService uploadService,
			SettingsService settingsService
	) {
		this.folderScannerService = folderScannerService;
		this.uploadService = uploadService;
		this.settingsService = settingsService;
		this.taskScheduler = new ThreadPoolTaskScheduler();
		this.taskScheduler.setPoolSize(1);
		this.taskScheduler.setThreadNamePrefix("rescan-scheduler-");
	}
	
	@PostConstruct
	public void start() {
		taskScheduler.initialize();
		reschedule(settingsService.getSettings().getRescanIntervalSeconds());
	}
	
    /**
     * Cancels the currently scheduled run (if any) and schedules a new one
     * at the given interval. Called both at startup and whenever the
     * settings page saves a new rescanIntervalSeconds value.
     */
    public synchronized void reschedule(int intervalSeconds) {
        if (currentSchedule != null) {
            currentSchedule.cancel(false);
        }
        Duration interval = Duration.ofSeconds(Math.max(10, intervalSeconds));
        currentSchedule = taskScheduler.scheduleWithFixedDelay(this::runCycle, interval);
        log.info("Rescan interval set to {} seconds", interval.getSeconds());
    }	

    private void runCycle() {
        try {
            folderScannerService.scanAll();
            uploadService.uploadAllDiscovered();
        } catch (Exception e) {
            // A single bad cycle (e.g. a transient filesystem issue)
            // shouldn't kill future scheduled runs -- log and let the next
            // scheduled cycle try again.
            log.error("Scan/upload cycle failed: {}", e.getMessage(), e);
        }
    }

    /** Triggers an immediate cycle, outside the normal schedule (e.g. a "scan now" button). */
    public void runNow() {
        taskScheduler.execute(this::runCycle);
    }
	
	
}