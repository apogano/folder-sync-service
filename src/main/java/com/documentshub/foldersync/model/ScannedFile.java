package com.documentshub.foldersync.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
	name = "scanned_file",
	indexes = {@Index(name = "idx_scanned_file_checksum",columnList = "checksum", unique = true)}
)
public class ScannedFile{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String folderPath;
	
	@Column(nullable = false)
	private String fileName;
	
	@Column(nullable = false)
	private String fullPath;
	
	@Column(nullable = false, unique = true)
	private String checksum;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ScanStatus status = ScanStatus.DISCOVERED;
	
	private int uploadAttempts = 0;
	
	private String errorMessage;
	
	private Instant discoveredAt = Instant.now();
	
	private Instant uploadedAt;
	
	protected ScannedFile() {}
	
	public ScannedFile(String folderPath, String fileName, String fullPath, String checksum) {
		this.folderPath = folderPath;
		this.fileName = fileName;
		this.fullPath = fullPath;
		this.checksum = checksum;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getFolderPath() {
		return folderPath;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getFullPath() {
		return fullPath;
	}
	
	public String getChecksum() {
		return checksum;
	}
	
	public ScanStatus getStatus() {
		return status;
	}
	
	public int getUploadAttempts() {
		return uploadAttempts;
	}
	
	public void incrementUploadAttempts() {
		this.uploadAttempts++;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public Instant getDiscoverdAt() {
		return discoveredAt;
	}
	
	public Instant getUploadedAt() {
		return uploadedAt;
	}
	
	public void markUploaded() {
		this.status = ScanStatus.UPLOADED;
		this.uploadedAt = Instant.now();
		this.errorMessage = null;
	}

	public void setStatus(ScanStatus status) {
		this.status = status;		
	}
}
