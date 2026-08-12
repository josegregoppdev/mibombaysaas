package com.josegregoppdev.mibombay.service.security;

import com.josegregoppdev.mibombay.common.util.AesKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

/**
 * AES-256-GCM encryption service for sensitive customer data (DNI, phone).
 *
 * Format: v1:ivBase64:cipherTextBase64
 *
 * IV is 12 bytes (96 bits) generated randomly per encryption.
 * GCM tag is 128 bits (default).
 */
@Service
@RequiredArgsConstructor
public class Aes256GcmEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String VERSION_PREFIX = "v1";

    private final AesKeyProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keyProvider.getAesKey(), spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return VERSION_PREFIX + ":"
                    + Base64.getEncoder().encodeToString(iv) + ":"
                    + Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt value", e);
        }
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null) {
            return null;
        }

        try {
            String[] parts = encryptedValue.split(":");
            if (parts.length != 3 || !VERSION_PREFIX.equals(parts[0])) {
                throw new IllegalArgumentException("Invalid encrypted value format");
            }

            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keyProvider.getAesKey(), spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (javax.crypto.AEADBadTagException e) {
            throw new IllegalStateException("Encrypted data has been tampered with or key is wrong", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt value", e);
        }
    }

    /**
     * HMAC-SHA256 lookup hash for searching/deduplication.
     * The plaintext is normalized (trimmed, uppercased) before hashing.
     * This allows exact-match lookups without exposing the plaintext.
     */
    public String computeLookupHash(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            String normalized = plaintext.trim().toUpperCase(Locale.ROOT);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keyProvider.getHmacKey());
            byte[] hash = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute lookup hash", e);
        }
    }
}
