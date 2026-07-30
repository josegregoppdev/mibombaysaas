package com.josegregoppdev.mibombay.controller.auth;

import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.crearAdmin;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordChangeControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordChangeController controller;

    @Test
    void mostrarFormulario_retornaVista() {
        assertEquals("cambiar-password", controller.mostrarFormulario());
    }

    @Test
    void cambiarPassword_passwordsNoCoinciden_retornaError() {
        Model model = new ExtendedModelMap();
        String view = controller.cambiarPassword("Password123!", "OtraPass456!", null, model);
        assertEquals("cambiar-password", view);
        assertTrue(model.containsAttribute("error"));
    }

    @Test
    void cambiarPassword_passwordMuyCorta_retornaError() {
        Model model = new ExtendedModelMap();
        String view = controller.cambiarPassword("Abc1!", "Abc1!", null, model);
        assertEquals("cambiar-password", view);
        assertTrue(model.containsAttribute("error"));
    }

    @Test
    void cambiarPassword_exitoso_redirigeADashboard() {
        Usuario usuario = crearAdmin();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(usuario.getEmail());

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$newHash");

        Model model = new ExtendedModelMap();
        String view = controller.cambiarPassword("Password123!", "Password123!", auth, model);
        assertEquals("redirect:/dashboard?passwordCambiada=true", view);
    }
}
