package com.documentshub.foldersync.web;

import com.documentshub.foldersync.model.ScannerSettings;
import com.documentshub.foldersync.model.WatchedFolder;
import com.documentshub.foldersync.repository.WatchedFolderRepository;
import com.documentshub.foldersync.service.SettingsService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SettingsController {

    private final SettingsService settingsService;
    private final WatchedFolderRepository watchedFolderRepository;
    
    public SettingsController(SettingsService settingsService,
                               WatchedFolderRepository watchedFolderRepository
    	) {
        this.settingsService = settingsService;
        this.watchedFolderRepository = watchedFolderRepository;
    }
    
    @GetMapping("/settings")
    public String settingsPage(Model model) {
    	model.addAttribute("settings",settingsService.getSettings());
    	model.addAttribute("folders", watchedFolderRepository.findAll());
    	return "settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute ScannerSettings formSettings) {
        ScannerSettings settings = settingsService.getSettings();
        settings.setUploadUrl(formSettings.getUploadUrl());
        settings.setUsername(formSettings.getUsername());

        if (formSettings.getPassword() != null && !formSettings.getPassword().isBlank()) {
            settings.setPassword(formSettings.getPassword());
        }
        settings.setWorkerCount(formSettings.getWorkerCount());
        settings.setRescanIntervalSeconds(formSettings.getRescanIntervalSeconds());
        settingsService.save(settings);

        return "redirect:/settings";
    }    
}