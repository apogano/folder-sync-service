package com.documentshub.foldersync.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.documentshub.foldersync.service.EncryptionService;

@Converter
public class PasswordEncryptor implements AttributeConverter<String, String> {

	//The better is to use Spring dependency injection (recommended)
	//TODO Change it later
    private static final String KEY = "change-it-in-production";

    private static final SecretKey SECRET_KEY =
            new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

    private static final EncryptionService encryptionService =
            new EncryptionService(SECRET_KEY);

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