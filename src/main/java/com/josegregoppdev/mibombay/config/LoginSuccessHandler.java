package com.josegregoppdev.mibombay.config;

import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.model.usuario.Rol;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (usuario.getDebeCambiarPassword()) {
            response.sendRedirect("/cambiar-password");
            return;
        }

        if (usuario.getRol() == Rol.SUPER_ADMINISTRADOR) {
            response.sendRedirect("/admin/dashboard");
            return;
        }

        String subdominioParam = request.getParameter("subdominio");
        if (subdominioParam == null || subdominioParam.isBlank()) {
            response.sendRedirect("/login?error=true");
            return;
        }

        Empresa empresa = empresaRepository.findBySubdominio(subdominioParam)
                .orElse(null);

        if (empresa == null || !empresa.getTenantId().equals(usuario.getTenantId())) {
            response.sendRedirect("/login?error=true");
            return;
        }

        String serverName = request.getServerName();
        String domain;
        if (serverName != null && serverName.split("\\.").length > 2) {
            domain = serverName.substring(serverName.indexOf('.') + 1);
        } else {
            domain = serverName != null ? serverName : "lvh.me";
        }
        int port = request.getServerPort();
        String scheme = request.getScheme();
        String redirect = scheme + "://" + subdominioParam + "." + domain
                + (port == 80 || port == 443 ? "" : ":" + port)
                + "/dashboard";
        response.sendRedirect(redirect);
    }
}
