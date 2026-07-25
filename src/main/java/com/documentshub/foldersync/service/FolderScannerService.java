package com.documentshub.foldersync.service;

import com.documentshub.foldersync.model.ScannedFile;
import com.documentshub.foldersync.model.WatchedFolder;
import com.documentshub.foldersync.repository.ScannedFileRepository;
import com.documentshub.foldersync.repository.WatchedFolderRepository;
import com.documentshub.foldersync.util.ChecksumUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.stream.Stream;

/**
 * Walks each configured folder and records any file whose content
 * (checksum) isn't already known. Deliberately a full re-walk each time
 * (not a persistent filesystem watch). This matches the "rescan every
 * N seconds" model from the settings UI, and is simpler to reason about
 * and make configurable at runtime than a live WatchService thread would
 * be. The trade-off (a change isn't noticed until the next rescan, not
 * instantly) is acceptable for a document-ingestion tool where files
 * arrive periodically, not in real time.
 */
@Service
public class FolderScannerService{
	
	private static final Logger log = LoggerFactory.getLogger(FolderScannerService.class);
	
	private final WatchedFolderRepository watchedFolderRepository;
	private final ScannedFileRepository scannedFileRepository;
	
	public FolderScannerService( 
			WatchedFolderRepository watchedFolderRepository,
			ScannedFileRepository scannedFileRepository
		) {
		this.watchedFolderRepository = watchedFolderRepository;
		this.scannedFileRepository = scannedFileRepository;
	}
	
	public void scanAll() {
		List<WatchedFolder> folders = watchedFolderRepository.findAll();
		for (WatchedFolder folder : folders) {
			scanFolder(folder.getPath());
		}
	}
	
	private void scanFolder(String folderPath) {
		Path root = Paths.get(folderPath);
		if (!Files.isDirectory(root)) {
			log.warn("Watched folder does not exist os is not a directory: {}",folderPath);
			return;
		}
		
		try (Stream<Path> paths = Files.walk(root)){
			paths.filter(Files::isRegularFile).forEach(file -> recordIfNew(folderPath, file));
		}
		catch (IOException e) {
			log.error("Failed to scan folder {}:{}",folderPath,e.getMessage());
		}
	}
	
	private void recordIfNew(String folderPath, Path file) {
		try {
			String checksum = ChecksumUtil.sha256(file);
			if (scannedFileRepository.findByChecksum(checksum).isPresent()) {
				return; //Already scanned and known. Same content already discovered.
			}
			ScannedFile scanFile = new ScannedFile(
				folderPath,
				file.getFileName().toString(),
				file.toAbsolutePath().toString(),
				checksum
			);
			scannedFileRepository.save(scanFile);
			log.info("Discovered new file: {}",file);					
		} 
		catch (IOException e) {
			log.warn("Could not checksum file {}: {}", file, e.getMessage());
		}
	}
	
}