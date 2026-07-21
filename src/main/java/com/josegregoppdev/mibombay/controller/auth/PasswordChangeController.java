package com.josegregoppdev.mibombay.controller.auth;

import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/cambiar-password")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String mostrarFormulario() {
        return "cambiar-password";
    }

    @PostMapping
    public String cambiarPassword(@RequestParam String nuevaPassword,
                                  @RequestParam String confirmarPassword,
                                  Authentication authentication,
                                  Model model) {
        if (!nuevaPassword.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "cambiar-password";
        }

        if (nuevaPassword.length() < 8) {
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres");
            return "cambiar-password";
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuario.setDebeCambiarPassword(false);
        usuario.setUltimoCambioPassword(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return "redirect:/dashboard?passwordCambiada=true";
    }
}
