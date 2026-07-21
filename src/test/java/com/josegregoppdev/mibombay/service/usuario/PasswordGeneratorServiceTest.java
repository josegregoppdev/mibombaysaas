package com.josegregoppdev.mibombay.service.usuario;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordGeneratorServiceTest {

    private final PasswordGeneratorService service = new PasswordGeneratorService();

    @Test
    void generarPasswordTemporal_longitudCorrecta() {
        String password = service.generarPasswordTemporal(12);
        assertEquals(12, password.length());
    }

    @Test
    void generarPasswordTemporal_sinParametros_longitudDefault12() {
        String password = service.generarPasswordTemporal();
        assertEquals(12, password.length());
    }

    @Test
    void generarPasswordTemporal_conLongitudMenor_retorna12() {
        String password = service.generarPasswordTemporal(4);
        assertEquals(12, password.length());
    }

    @Test
    void generarPasswordTemporal_contieneMayuscula() {
        String password = service.generarPasswordTemporal();
        assertTrue(password.chars().anyMatch(Character::isUpperCase));
    }

    @Test
    void generarPasswordTemporal_contieneMinuscula() {
        String password = service.generarPasswordTemporal();
        assertTrue(password.chars().anyMatch(Character::isLowerCase));
    }

    @Test
    void generarPasswordTemporal_contieneDigito() {
        String password = service.generarPasswordTemporal();
        assertTrue(password.chars().anyMatch(Character::isDigit));
    }

    @Test
    void generarPasswordTemporal_contieneSimbolo() {
        String password = service.generarPasswordTemporal();
        assertTrue(password.chars().anyMatch(ch -> "!@#$%^&*()-_=+".indexOf(ch) >= 0));
    }

    @Test
    void generarPasswordTemporal_passwordsAleatorias() {
        String pass1 = service.generarPasswordTemporal();
        String pass2 = service.generarPasswordTemporal();
        assertNotEquals(pass1, pass2);
    }
}
