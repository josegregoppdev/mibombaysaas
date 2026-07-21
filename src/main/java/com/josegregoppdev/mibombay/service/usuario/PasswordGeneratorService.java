package com.josegregoppdev.mibombay.service.usuario;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;

    private final SecureRandom random = new SecureRandom();

    public String generarPasswordTemporal(int longitud) {
        if (longitud < 12) {
            longitud = 12;
        }

        StringBuilder password = new StringBuilder();

        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        for (int i = 4; i < longitud; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    public String generarPasswordTemporal() {
        return generarPasswordTemporal(12);
    }
}
