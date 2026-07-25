package com.documentshub.foldersync.repository;

import com.documentshub.foldersync.model.ScannedFile;
import com.documentshub.foldersync.model.ScanStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScannedFileRepository extends JpaRepository<ScannedFile, Long>{
	Optional<ScannedFile> findByChecksum(String checksum);
	List<ScannedFile> findByStatus(ScanStatus status);
	List<ScannedFile> findAllByOrderByDiscoveredAtDesc();
}