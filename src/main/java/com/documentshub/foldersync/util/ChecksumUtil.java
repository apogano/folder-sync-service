package com.documentshub.foldersync.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ChecksumUtil{
	
	private ChecksumUtil() {}
	
    /**
     * Computes SHA-256 over the file's actual content, not its metadata --
     * this is what lets the scanner (and the server, independently) detect
     * "is this the same file I've already uploaded" regardless of
     * filename, path, or timestamps.
     */
	public static String sha256(Path file) throws IOException{
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			
			try (InputStream in = Files.newInputStream(file)){
				byte[] buffer = new byte[8192];
				int read;
				while ((read = in.read(buffer)) != -1 ) {
					digest.update(buffer, 0, read);
				}
			}
			byte[] hashBytes = digest.digest();
			StringBuilder hex = new StringBuilder(hashBytes.length * 2);
			for (byte b : hashBytes) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		}
		catch (NoSuchAlgorithmException e) {
			// SHA-256 is guaranteed available on every standard JVM.
			// This catch is unreachable in practice, but the checked
			// exception must still be handled.
			throw new IllegalStateException("SHA-256 algorithm not available", e);
		}
	}
}