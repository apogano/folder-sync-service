package com.documentshub.foldersync.upload;

import com.documentshub.foldersync.auth.AuthTokenService;
import com.documentshub.foldersync.service.SettingsService;
import com.documentshub.foldersync.model.ScannerSettings;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.File;

/**
 * Performs one upload attempt. Retry/backoff is handled by the caller
 * (UploadService) -- this class's job is just "make one HTTP call,
 * succeed or throw", kept deliberately simple and single-purpose.
 */
@Component
public class DocumentUploadClient{
	
	private final SettingsService settingsService;
	
	private final AuthTokenService authTokenService;
	
	private final RestClient.Builder restClientBuilder;
	
	public DocumentUploadClient(SettingsService settingsService, AuthTokenService authTokenService,
			RestClient.Builder restClientBuilder ) {
		this.settingsService = settingsService;
		this.authTokenService = authTokenService;
		this.restClientBuilder = restClientBuilder;
	}
	
	public void upload(File file) {
		try {		
			ScannerSettings settings = settingsService.getSettings();
	        RestClient client = restClientBuilder
	                .baseUrl(settings.getUploadUrl())
	                .build();  
	        
			String token = authTokenService.getValidAccessToken();
			
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
			body.add("file", new FileSystemResource(file));

			client.post()
			      .uri("/api/documents/upload")
			      .header("Authorization", "Bearer "+token)
			      .contentType(MediaType.MULTIPART_FORM_DATA)
			      .body(body)
			      .retrieve()
			      .toBodilessEntity();
           // No exception here means a 2xx response (200 duplicate-exists,
           // or 202 accepted) -- RestClient's default retrieve() only
           // throws for 4xx/5xx statuses, so both success cases are
           // already handled by simply not throwing.			
		} catch (RestClientResponseException e) {
			throw new UploadException(
					"Upload failed with status " + e.getStatusCode() + ": "+ e.getResponseBodyAsString(), e
			);
		} catch (Exception e) {
			throw new UploadException("Upload failed:" + e.getMessage(),e);
		}
	}
}
