package com.documentshub.foldersync.repository;

import com.documentshub.foldersync.model.WatchedFolder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WatchedFolderRepository extends JpaRepository<WatchedFolder, Long>{
	Optional<WatchedFolder> findByPath(String path);
}