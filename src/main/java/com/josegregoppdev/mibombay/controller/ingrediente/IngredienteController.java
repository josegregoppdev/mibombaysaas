package com.josegregoppdev.mibombay.controller.ingrediente;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.ingrediente.IngredienteDTO;
import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import com.josegregoppdev.mibombay.service.ingrediente.IngredienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ingrediente")
@RequiredArgsConstructor
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @GetMapping
    public String listar(@PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC)
                         Pageable pageable, Model model) {
        model.addAttribute("pagina", ingredienteService.listar(tenantId(), pageable));
        return "ingrediente/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("ingrediente", new IngredienteDTO());
        model.addAttribute("categorias", Categoria.values());
        return "ingrediente/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("ingrediente") IngredienteDTO dto,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "ingrediente/formulario";
        }

        try {
            ingredienteService.crear(dto, tenantId());
            redirectAttributes.addFlashAttribute("mensaje", "Ingrediente creado correctamente");
            return "redirect:/ingrediente";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingrediente/nuevo";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarEdicion(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("ingrediente", ingredienteService.obtenerPorId(id, tenantId()));
            model.addAttribute("categorias", Categoria.values());
            return "ingrediente/formulario";
        } catch (IllegalArgumentException e) {
            return "redirect:/ingrediente";
        }
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("ingrediente") IngredienteDTO dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "ingrediente/formulario";
        }

        try {
            ingredienteService.actualizar(id, dto, tenantId());
            redirectAttributes.addFlashAttribute("mensaje", "Ingrediente actualizado correctamente");
            return "redirect:/ingrediente";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingrediente/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            ingredienteService.desactivar(id, tenantId());
            redirectAttributes.addFlashAttribute("mensaje", "Ingrediente desactivado correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ingrediente";
    }

    private String tenantId() {
        return TenantContext.get();
    }
}
