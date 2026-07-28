package com.josegregoppdev.mibombay.controller.admin;

import com.josegregoppdev.mibombay.model.empresa.Empresa;
import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmpresaRepository empresaRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        long total = empresas.size();
        long activas = empresas.stream().filter(Empresa::getActivo).count();
        long inactivas = total - activas;

        model.addAttribute("total", total);
        model.addAttribute("activas", activas);
        model.addAttribute("inactivas", inactivas);
        return "admin/dashboard";
    }

    @GetMapping("/empresas")
    public String empresas(Model model) {
        List<Empresa> empresas = empresaRepository.findAll();
        model.addAttribute("empresas", empresas);
        return "admin/empresas";
    }
}
