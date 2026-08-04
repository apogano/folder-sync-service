package com.documentshub.foldersync.auth;

public record LoginRequest(
		String username,
		String password
) {}