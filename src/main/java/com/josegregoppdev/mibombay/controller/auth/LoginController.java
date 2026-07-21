package com.josegregoppdev.mibombay.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Email o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente");
        }
        return "login";
    }
}
