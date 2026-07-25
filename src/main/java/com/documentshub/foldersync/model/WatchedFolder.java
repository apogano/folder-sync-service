package com.documentshub.foldersync.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class WatchedFolder{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ;
	
	private String path;
	
	protected WatchedFolder() {}
	
	public WatchedFolder(String path) {
		this.path = path;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
}