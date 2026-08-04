package com.documentshub.foldersync.auth;

import com.documentshub.foldersync.model.ScannerSettings;
import com.documentshub.foldersync.service.SettingsService;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the scanner's JWT session against documents-hub: logs in with the
 * configured username/password, keeps the access token refreshed, and
 * transparently re-logs-in if the refresh token itself has also expired.
 *
 * documents-hub's upload endpoint requires a real user session (JWT), not a
 * static API key, so every uploading worker thread needs a currently-valid
 * access token -- this class is the single, thread-safe source of that
 * token for the whole application.
 */
@Service
public class AuthTokenService{
	
	private static final long EXPIRY_SAFETY_MARGIN_SECONDS = 30;
	
	private final SettingsService settingsService;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private volatile String accessToken;
	private volatile String refreshToken;
	private volatile Instant accessTokenExpiresAt = Instant.EPOCH;
	
	public AuthTokenService(SettingsService settingsService) {
		this.settingsService = settingsService;
	}
	
	/**
	 * Returns a currently valid access token, logging in or refreshing as needed.
	 * Synchronized so concurrent upload workers don't each trigger their own 
	 * login/refresh call at the same time.
	 */
	public synchronized String getValidAccessToken() {
		if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt.minusSeconds(EXPIRY_SAFETY_MARGIN_SECONDS))) {
			return accessToken;
		}
		if (refreshToken != null) {
			try {
				refresh();
				return accessToken;
			}
			catch (Exception e) {
               // Refresh token itself may have expired. Fall through to a
               // fresh login rather than failing the caller outright.				
			}
		}
		login();
		return accessToken;
	}
	
	private void login() {
        ScannerSettings settings = settingsService.getSettings();
        //RestClient client = RestClient.create(settings.getUploadUrl());
        RestClient client = RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    return execution.execute(request, body);
                })
                .baseUrl(settings.getUploadUrl())
                .build();
        
        LoginRequest request = new LoginRequest(
                settings.getUsername(),
                settings.getPassword()
        );
        
        TokenResponse response = client.post()
                .uri("/api/token/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TokenResponse.class);
        
        applyTokenResponse(response);
    }	
    
	private void refresh() {
		ScannerSettings settings = settingsService.getSettings();
	       RestClient client = RestClient.builder()
	                .requestInterceptor((request, body, execution) -> {
	                    return execution.execute(request, body);
	                })
	                .baseUrl(settings.getUploadUrl())
	                .build();
		
		TokenResponse response = client.post()
				.uri("/api/token/refresh/")
				.body(Map.of("refresh",refreshToken))
				.retrieve()
				.body(TokenResponse.class);
		System.out.println(response);
		this.accessToken = response.access();
		this.refreshToken = response.refresh();
	}
	
	private void applyTokenResponse(TokenResponse response) {
		this.accessToken = response.access();
		this.refreshToken = response.refresh();
		this.accessTokenExpiresAt = extractExpiry(response.access());
	}
	
	/**
	 * JWTs are self-describing: the payload (middle segment) carries an 
	 * "exp" claim. Decoding it directly means we track the *actual* expiry 
	 * the server set, rather than quessing a token lifetime that could drift out
	 * of sync with server config.
	 * @param jwt
	 * @return
	 */
	private Instant extractExpiry(String jwt) {
		try {
			String[] parts = jwt.split("\\,");
			String payloadJson = new String(
				Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8	
			);
			Map<?, ?> claims = objectMapper.readValue(payloadJson, Map.class);
			long expEpochSeconds = ((Number) claims.get("exp")).longValue();
			return Instant.ofEpochSecond(expEpochSeconds);					
		}
		catch (Exception e) {
			// If the token can't be parsed for any reason, treat is as
			// already expired. Then a fresh login is forced rather than silently 
			// trusting a token we couldn't verify.
			return Instant.EPOCH;
		}
	}
}