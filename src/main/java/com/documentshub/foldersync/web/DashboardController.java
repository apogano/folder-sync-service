package com.documentshub.foldersync.web;

import com.documentshub.foldersync.model.ScanStatus;
import com.documentshub.foldersync.model.ScannedFile;
import com.documentshub.foldersync.repository.ScannedFileRepository;
import com.documentshub.foldersync.service.RescanSchedulerService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DashboardController{
	
	private final ScannedFileRepository scannedFileRepository;
	private final RescanSchedulerService rescanSchedulerService;
	
	public DashboardController(ScannedFileRepository scannedFileRepository,
			RescanSchedulerService rescanSchedulerService  ){
		this.scannedFileRepository = scannedFileRepository;
	    this.rescanSchedulerService = rescanSchedulerService;
		
	}
	
	@GetMapping("/")
	public String dashboard(Model model) {
		model.addAttribute("files",scannedFileRepository.findAllByOrderByDiscoveredAtDesc());
		return "dashboard";
	}
	
	@PostMapping("/scan-now")
	public String scanNow() {
		rescanSchedulerService.runNow();
		return "redirect:/";
	}
	
    @PostMapping("/{id}/mark-as-discovered")
    public String markAsDiscovered(@PathVariable Long id) {
    	ScannedFile scannedFile = scannedFileRepository.findById(id).orElse(null);
    	scannedFile.setStatus(ScanStatus.DISCOVERED);
    	scannedFile.setErrorMessage(null);
        scannedFileRepository.save(scannedFile);
        return "redirect:/";
    } 	
	
	
	
}
