package com.josegregoppdev.mibombay.controller.ingrediente;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.ingrediente.IngredienteDTO;
import com.josegregoppdev.mibombay.model.ingrediente.Categoria;
import com.josegregoppdev.mibombay.model.ingrediente.UnidadMedida;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ingrediente")
@RequiredArgsConstructor
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @GetMapping
    public String listar(@RequestParam(required = false) String nombre,
                         @RequestParam(required = false) Categoria categoria,
                         @RequestParam(required = false) UnidadMedida unidadMedida,
                         @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC)
                         Pageable pageable, Model model) {
        model.addAttribute("pagina", ingredienteService.obtenerIngredientesPaginados(
                tenantId(), nombre, categoria, unidadMedida, pageable));
        model.addAttribute("nombre", nombre);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("unidadMedidaSeleccionada", unidadMedida);
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("unidadesMedida", UnidadMedida.values());
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
            ingredienteService.crearNuevoIngrediente(dto, tenantId());
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
            model.addAttribute("ingrediente", ingredienteService.obtenerIngredientePorId(id, tenantId()));
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
            ingredienteService.actualizarIngredienteExistente(id, dto, tenantId());
            redirectAttributes.addFlashAttribute("mensaje", "Ingrediente actualizado correctamente");
            return "redirect:/ingrediente";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingrediente/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/toggle-estado")
    public String toggleEstado(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            ingredienteService.cambiarEstadoActivoDelIngrediente(id, tenantId());
            redirectAttributes.addFlashAttribute("mensaje", "Estado del ingrediente actualizado correctamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ingrediente";
    }

    private String tenantId() {
        return TenantContext.get();
    }
}
