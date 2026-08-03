package com.documentshub.foldersync.service;

import com.documentshub.foldersync.model.ScannerSettings;
import com.documentshub.foldersync.model.ScanStatus;
import com.documentshub.foldersync.model.ScannedFile;
import com.documentshub.foldersync.repository.ScannedFileRepository;
import com.documentshub.foldersync.upload.DocumentUploadClient;
import com.documentshub.foldersync.upload.UploadException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;


/**
 * Finds DISCOVERED files and dispatches each one to the (dynamically
 * sized) worker pool for upload, with exponential backoff between retry
 * attempts within each worker.
 */
@Service
public class UploadService{
	
	private static final Logger log = LoggerFactory.getLogger(UploadService.class);
	private static final int MAX_ATTEMPTS = 5;
	private static final long INITIAL_DELAY_MILLIS = 1000;
	private static final long MAX_DELAY_MILLIS = 60_000;
	
	private final ScannedFileRepository scannedFileRepository;
	private final DocumentUploadClient uploadClient;
	private final UploadExecutorManager executorManager;
	
	public UploadService(
			ScannedFileRepository scannedFileRepository,
			DocumentUploadClient uploadClient,
			UploadExecutorManager executorManager
			) {
		this.scannedFileRepository = scannedFileRepository;
		this.uploadClient = uploadClient;
		this.executorManager = executorManager;
	}
	
	public void uploadAllDiscovered() {
		List<ScannedFile> discovered = scannedFileRepository.findByStatus(ScanStatus.DISCOVERED);
		for (ScannedFile file : discovered) {
			executorManager.submit(() -> uploadWithRetry(file.getId()));
		}
	}

	/**
	 * Re-fetches the entity by id at the start (rather than closing over
	 * the original instance) since this runs on a worker thread, possibly
	 * some time after uploadAllDiscovered() originally queried it --
	 * avoids acting on stale state.
	 */
	private void uploadWithRetry(Long scannedFileId) {
		ScannedFile file = scannedFileRepository.findById(scannedFileId).orElse(null);
		if (file == null) {
			return;
		}
		
		file.setStatus(ScanStatus.UPLOADING);
		scannedFileRepository.save(file);
		
		long delay = INITIAL_DELAY_MILLIS;
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			file.incrementUploadAttempts();
			try {
				uploadClient.upload(new File(file.getFullPath()));
				file.markUploaded();
				scannedFileRepository.save(file);
				log.info("Uploaded {} (attempt {})",file.getFileName(),attempt);
				return;
			} catch (UploadException e) {
				log.warn("Upload attempt {}/{} failed for {}:{}",
						attempt,MAX_ATTEMPTS,file.getFileName(),e.getMessage());
				file.setErrorMessage(e.getMessage());
				scannedFileRepository.save(file);
				
				if (attempt == MAX_ATTEMPTS) {
					file.setStatus(ScanStatus.FAILED);
					scannedFileRepository.save(file);
					log.error("Giving up on {} after {} attempts", 
							file.getFileName(),MAX_ATTEMPTS);
					return;
				}
			}
			
			sleep(delay);
            // Exponential backoff, capped -- same shape as the backoff
            // used on the documents-hub/Celery side of this system,
            // arrived at independently here since Java's retry tooling
            // doesn't give this "for free" the way Celery's decorator
            // options do.
            delay = Math.min(delay * 2, MAX_DELAY_MILLIS);			
		}
	}
	
	private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
	
	
}