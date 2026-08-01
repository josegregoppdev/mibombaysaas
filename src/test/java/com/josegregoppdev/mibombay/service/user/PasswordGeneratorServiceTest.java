package com.josegregoppdev.mibombay.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordGeneratorServiceTest {

    private final PasswordGeneratorService service = new PasswordGeneratorService();

    @Test
    void generateTemporaryPassword_correctLength() {
        String password = service.generateTemporaryPassword(12);
        assertEquals(12, password.length());
    }

    @Test
    void generateTemporaryPassword_noParams_defaultLength12() {
        String password = service.generateTemporaryPassword();
        assertEquals(12, password.length());
    }

    @Test
    void generateTemporaryPassword_withSmallLength_returns12() {
        String password = service.generateTemporaryPassword(4);
        assertEquals(12, password.length());
    }

    @Test
    void generateTemporaryPassword_containsUppercase() {
        String password = service.generateTemporaryPassword();
        assertTrue(password.chars().anyMatch(Character::isUpperCase));
    }

    @Test
    void generateTemporaryPassword_containsLowercase() {
        String password = service.generateTemporaryPassword();
        assertTrue(password.chars().anyMatch(Character::isLowerCase));
    }

    @Test
    void generateTemporaryPassword_containsDigit() {
        String password = service.generateTemporaryPassword();
        assertTrue(password.chars().anyMatch(Character::isDigit));
    }

    @Test
    void generateTemporaryPassword_containsSymbol() {
        String password = service.generateTemporaryPassword();
        assertTrue(password.chars().anyMatch(ch -> "!@#$%^&*()-_=+".indexOf(ch) >= 0));
    }

    @Test
    void generateTemporaryPassword_randomPasswords() {
        String pass1 = service.generateTemporaryPassword();
        String pass2 = service.generateTemporaryPassword();
        assertNotEquals(pass1, pass2);
    }
}
