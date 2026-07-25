package com.documentshub.foldersync.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Convert;

/**
 * Single-row table holding all runtime-editable configuration.
 * There is deliverately only ever one row(id is always fixed at SINGLETON_ID).
 * This is a-per-instance scanner agent, not a multi-tenant system, so a full settings table 
 * with a lookup key would be unnecessary complexity.
 * 
 * Editable from the /settings page in the dashboard.
 */
@Entity
public class ScannerSettings{
	public static final long SINGLETON_ID = 1L;
	
	@Id 
	private Long id = SINGLETON_ID;
	
	private String uploadUrl = "http://localhost:8000";
	
	private String username = "";
	
	@Convert(converter = PasswordEncryptor.class)
	private String password = "";
	
	private int workerCount = 2;
	
	private int rescanIntervalSeconds = 300;
	
	public Long getId() {
		return id;
	}
	
	public String getUploadUrl() {
		return uploadUrl;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public int getWorkerCount() {
		return workerCount;
	}
	
	public void setWorkerCount(int workerCount) {
		this.workerCount = Math.max(1, workerCount);
	}
	
	public int getRescanIntervalSeconds() {
		return rescanIntervalSeconds;
	}
	
	public void setRescanIntervalSeconds(int rescanIntervalSeconds) {
		this.rescanIntervalSeconds = rescanIntervalSeconds;
	}
	
}