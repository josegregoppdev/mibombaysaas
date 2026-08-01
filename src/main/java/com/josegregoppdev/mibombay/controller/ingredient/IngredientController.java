package com.josegregoppdev.mibombay.controller.ingredient;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.ingredient.IngredientDTO;
import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import com.josegregoppdev.mibombay.service.ingredient.IngredientService;
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
@RequestMapping("/ingredient")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) Category category,
                       @RequestParam(required = false) UnitOfMeasure unitOfMeasure,
                       @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
                       Pageable pageable, Model model) {
        model.addAttribute("page", ingredientService.getPaginatedIngredients(
                tenantId(), name, category, unitOfMeasure, pageable));
        model.addAttribute("name", name);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedUnitOfMeasure", unitOfMeasure);
        model.addAttribute("categories", Category.values());
        model.addAttribute("unitsOfMeasure", UnitOfMeasure.values());
        return "ingredient/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("ingredient", new IngredientDTO());
        model.addAttribute("categories", Category.values());
        return "ingredient/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("ingredient") IngredientDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "ingredient/form";
        }

        try {
            ingredientService.createNewIngredient(dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Ingredient created successfully");
            return "redirect:/ingredient";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingredient/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("ingredient", ingredientService.getIngredientById(id, tenantId()));
            model.addAttribute("categories", Category.values());
            return "ingredient/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/ingredient";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("ingredient") IngredientDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "ingredient/form";
        }

        try {
            ingredientService.updateExistingIngredient(id, dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Ingredient updated successfully");
            return "redirect:/ingredient";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ingredient/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            ingredientService.toggleIngredientActiveStatus(id, tenantId());
            redirectAttributes.addFlashAttribute("message", "Ingredient status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ingredient";
    }

    private String tenantId() {
        return TenantContext.get();
    }
}
