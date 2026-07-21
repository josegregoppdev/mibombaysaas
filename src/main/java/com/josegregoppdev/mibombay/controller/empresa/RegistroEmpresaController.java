package com.josegregoppdev.mibombay.controller.empresa;

import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.service.empresa.RegistroEmpresaService;
import com.josegregoppdev.mibombay.service.empresa.RegistroEmpresaService.RegistroEmpresaResultado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/registro")
@RequiredArgsConstructor
public class RegistroEmpresaController {

    private final RegistroEmpresaService registroEmpresaService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "registro";
    }

    @PostMapping
    public String procesarRegistro(@Valid @ModelAttribute("empresa") Empresa empresa,
                                   BindingResult bindingResult,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            return "registro";
        }

        try {
            RegistroEmpresaResultado resultado = registroEmpresaService.registrar(empresa);
            model.addAttribute("resultado", resultado);
            return "registro-exitoso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }
}
