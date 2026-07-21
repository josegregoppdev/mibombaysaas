package com.josegregoppdev.mibombay.controller.empresa;

import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTORequest;
import com.josegregoppdev.mibombay.dto.empresa.EmpresaDTOResponse;
import com.josegregoppdev.mibombay.service.empresa.RegistroEmpresaService;
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
        model.addAttribute("empresaRequest", new EmpresaDTORequest());
        return "registro";
    }

    @PostMapping
    public String procesarRegistro(@Valid @ModelAttribute("empresaRequest") EmpresaDTORequest empresaRequest,
                                   BindingResult bindingResult,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            return "registro";
        }

        try {
            EmpresaDTOResponse resultado = registroEmpresaService.registrar(empresaRequest);
            model.addAttribute("resultado", resultado);
            return "registro-exitoso";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }
}
