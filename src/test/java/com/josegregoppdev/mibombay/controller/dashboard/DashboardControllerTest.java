package com.josegregoppdev.mibombay.controller.dashboard;

import com.josegregoppdev.mibombay.model.usuario.Rol;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private DashboardController controller;

    @Test
    void dashboard_retornaVistaConDatos() {
        Usuario usuario = crearAdmin();
        var empresa = crearEmpresa();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(usuario.getEmail());

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(empresaRepository.findByTenantId(usuario.getTenantId())).thenReturn(Optional.of(empresa));

        Model model = new ExtendedModelMap();
        String view = controller.dashboard(auth, null, model);

        assertEquals("dashboard", view);
        assertTrue(model.containsAttribute("usuario"));
        assertTrue(model.containsAttribute("empresa"));
    }

    @Test
    void dashboard_conPasswordCambiada_agregaMensaje() {
        Usuario usuario = crearAdmin();
        var empresa = crearEmpresa();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(usuario.getEmail());

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(empresaRepository.findByTenantId(usuario.getTenantId())).thenReturn(Optional.of(empresa));

        Model model = new ExtendedModelMap();
        String view = controller.dashboard(auth, "true", model);

        assertEquals("dashboard", view);
        assertTrue(model.containsAttribute("mensaje"));
    }

    @Test
    void dashboard_debeCambiarPassword_redirige() {
        Usuario usuario = crearAdmin();
        usuario.setDebeCambiarPassword(true);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(usuario.getEmail());

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        Model model = new ExtendedModelMap();
        String view = controller.dashboard(auth, null, model);

        assertEquals("redirect:/cambiar-password", view);
    }

    @Test
    void dashboard_superAdmin_redirigeAAdmin() {
        Usuario superAdmin = crearSuperAdmin();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(superAdmin.getEmail());

        when(usuarioRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        Model model = new ExtendedModelMap();
        String view = controller.dashboard(auth, null, model);

        assertEquals("redirect:/admin/dashboard", view);
    }
}
