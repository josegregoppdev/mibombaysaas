package com.josegregoppdev.mibombay.controller.dashboard;

import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    @GetMapping
    public String dashboard(Authentication authentication,
                            @RequestParam(required = false) String passwordCambiada,
                            Model model) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (usuario.getDebeCambiarPassword()) {
            return "redirect:/cambiar-password";
        }

        Empresa empresa = empresaRepository.findByTenantId(usuario.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Empresa no encontrada"));

        model.addAttribute("usuario", usuario);
        model.addAttribute("empresa", empresa);

        if (passwordCambiada != null) {
            model.addAttribute("mensaje", "Contraseña cambiada correctamente");
        }

        return "dashboard";
    }
}
