package com.documentshub.foldersync.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Wraps a ThreadPoolTaskExecutor whose size can change at runtime, without
 * a restart -- this is what makes the "worker count" field in the settings
 * UI actually take effect immediately. Spring's ThreadPoolTaskExecutor
 * exposes setCorePoolSize()/setMaxPoolSize() as live-mutable properties on
 * the underlying java.util.concurrent.ThreadPoolExecutor, so resizing is a
 * genuinely supported operation, not a hack.
 */
@Component
public class UploadExecutorManager{
	
	private static final Logger log = LoggerFactory.getLogger(UploadExecutorManager.class);
	
	private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	
	@PostConstruct
	public void init() {
		executor.setThreadNamePrefix("upload-worker-");
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.initialize();
	}
	
	/**
	 * Resizes the upload worker pool at runtime without restarting the application.
	 * The order of updating max and core pool sizes matters because ThreadPoolExecutor
	 * requires corePoolSize to never exceed maxPoolSize. When increasing the pool,
	 * max size must be updated first; when decreasing it, core size must be reduced first.
	 */
	public void resize(int workerCount) {
		int safeCount = Math.max(1, workerCount);
		int currentMax = executor.getMaxPoolSize();
		if (safeCount > currentMax) {
			executor.setMaxPoolSize(safeCount);	
			executor.setCorePoolSize(safeCount);
		}
		else {
	        executor.setCorePoolSize(safeCount);
	        executor.setMaxPoolSize(safeCount);	
		}
		
		log.info("Upload worker pool resized to {}",safeCount);
	}
	
	public void submit(Runnable task) {
		executor.execute(task);
	}
}