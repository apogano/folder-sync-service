package com.documentshub.foldersync.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.documentshub.foldersync.service.EncryptionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Converter
public class PasswordEncryptor implements AttributeConverter<String, String> {

	private static final Logger log = LoggerFactory.getLogger(PasswordEncryptor.class);
	
	private static final String ENV_VAR_NAME = "SCANNER_ENCRYPTION_KEY";
	
    // Used only if SCANNER_ENCRYPTION_KEY isn't set. Lets the app still
    // start and work for local development, but is NOT safe for any real
    // deployment -- anyone with this source code can derive it.
    private static final String DEV_ONLY_FALLBACK_KEY = "0123456789abcdef0123456789abcdef";
    
    private static final SecretKey SECRET_KEY = buildSecretKey();

    private static final EncryptionService encryptionService =
            new EncryptionService(SECRET_KEY);

    private static SecretKey buildSecretKey() {
        String configured = System.getenv(ENV_VAR_NAME);
        String keyMaterial;
        if (configured == null || configured.isBlank()) {
            log.warn("{} is not set -- using an insecure, hardcoded development-only "
                    + "encryption key. Set this environment variable before storing any "
                    + "real credentials; without it, anyone with this source code can "
                    + "decrypt stored passwords.", ENV_VAR_NAME);
            keyMaterial = DEV_ONLY_FALLBACK_KEY;
        } else {
            keyMaterial = configured;
        }
        return new SecretKeySpec(normalizeTo32Bytes(keyMaterial), "AES");
    }

    private static byte[] normalizeTo32Bytes(String key) {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[32]; // AES-256 key size
        System.arraycopy(raw, 0, normalized, 0, Math.min(raw.length, 32));
        return normalized;
    }
 
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        return encryptionService.decrypt(dbData);
    }
}