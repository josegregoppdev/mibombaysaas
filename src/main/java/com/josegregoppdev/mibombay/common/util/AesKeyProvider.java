package com.josegregoppdev.mibombay.common.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Getter
public class AesKeyProvider {

    private final SecretKey aesKey;
    private final SecretKey hmacKey;

    public AesKeyProvider(
            @Value("${mibombay.security.customer-encryption-key}") String base64Key) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        if (decoded.length != 32) {
            throw new IllegalStateException(
                    "Customer encryption key must be exactly 32 bytes (256-bit) when Base64-decoded, got " + decoded.length);
        }
        this.aesKey = new SecretKeySpec(decoded, "AES");

        byte[] hmacDerived = new byte[32];
        System.arraycopy(decoded, 0, hmacDerived, 0, 32);
        this.hmacKey = new SecretKeySpec(hmacDerived, "HmacSHA256");
    }
}
