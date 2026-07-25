package com.documentshub.foldersync.auth;

/**
 * Maps directly to the JSON body returned by documents-hub's
 * POST /api/token/ and /api/token/refresh/ endpoints (djangorestframework-simplejwt's
 * default response shape).
 */
public record TokenResponse(String access, String refresh) {
	
}