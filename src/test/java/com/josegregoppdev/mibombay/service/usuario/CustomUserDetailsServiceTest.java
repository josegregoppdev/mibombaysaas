package com.josegregoppdev.mibombay.service.usuario;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void loadUserByUsername_usuarioExiste_retornaUserDetails() {
        Usuario usuario = crearAdmin();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        UserDetails userDetails = service.loadUserByUsername(usuario.getEmail());

        assertNotNull(userDetails);
        assertEquals(usuario.getEmail(), userDetails.getUsername());
        assertEquals(usuario.getPasswordHash(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_usuarioNoExiste_lanzaExcepcion() {
        String email = "noexiste@test.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(email));
    }

    @Test
    void loadUserByUsername_usuarioInactivo_lanzaExcepcion() {
        Usuario usuario = crearAdmin();
        usuario.setActivo(false);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(usuario.getEmail()));
    }

    @Test
    void loadUserByUsername_seteaTenantIdEnContext() {
        Usuario usuario = crearAdmin();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        service.loadUserByUsername(usuario.getEmail());

        assertEquals(usuario.getTenantId(), TenantContext.get());
    }
}
