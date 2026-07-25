package com.documentshub.foldersync.repository;

import com.documentshub.foldersync.model.ScannerSettings;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScannerSettingsRepository extends JpaRepository<ScannerSettings, Long>{
	
}