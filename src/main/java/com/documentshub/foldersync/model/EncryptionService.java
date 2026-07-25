package com.documentshub.foldersync.model;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionService {

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKey secretKey;

    public EncryptionService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encrypted) {
        try {
            byte[] data = Base64.getDecoder().decode(encrypted);

            byte[] iv = new byte[IV_LENGTH];
            byte[] ciphertext = new byte[data.length - IV_LENGTH];

            System.arraycopy(data, 0, iv, 0, IV_LENGTH);
            System.arraycopy(data, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

            return new String(cipher.doFinal(ciphertext));

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}