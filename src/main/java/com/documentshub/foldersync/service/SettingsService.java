package com.documentshub.foldersync.service;

import com.documentshub.foldersync.model.ScannerSettings;
import com.documentshub.foldersync.repository.ScannerSettingsRepository;

import org.springframework.stereotype.Service;

/**
 * Central access point for the single ScannerSettings row. 
 * Creates it with deafults of the first run if it doesn't exist yet,
 * so the app never has to handle "settings might not exist"
 */
@Service
public class SettingsService {
	
	private final ScannerSettingsRepository repository;
	
	public SettingsService(ScannerSettingsRepository repository) {
		this.repository = repository;
	}
	
	public ScannerSettings getSettings() {
		return repository.findById(ScannerSettings.SINGLETON_ID)
				.orElseGet(() -> repository.save(new ScannerSettings()));
	}
	
	public ScannerSettings save(ScannerSettings settings) {
		return repository.save(settings);
	}
	
	
}