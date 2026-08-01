package com.josegregoppdev.mibombay.controller.recipe;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.recipe.RecipeDTO;
import com.josegregoppdev.mibombay.dto.recipe.RecipeDetailDTO;
import com.josegregoppdev.mibombay.service.ingredient.IngredientService;
import com.josegregoppdev.mibombay.service.recipe.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final IngredientService ingredientService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
                       Pageable pageable, Model model) {
        model.addAttribute("page", recipeService.getPaginatedRecipes(tenantId(), name, pageable));
        model.addAttribute("name", name);
        return "recipe/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        RecipeDTO dto = new RecipeDTO();
        List<RecipeDetailDTO> details = new ArrayList<>();
        details.add(new RecipeDetailDTO());
        dto.setDetails(details);
        model.addAttribute("recipe", dto);
        model.addAttribute("ingredients", ingredientService.getPaginatedActiveIngredients(tenantId(), PageRequest.of(0, 1000)));
        return "recipe/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("recipe") RecipeDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ingredients", ingredientService.getPaginatedActiveIngredients(tenantId(), PageRequest.of(0, 1000)));
            return "recipe/form";
        }
        try {
            recipeService.createNewRecipe(dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Recipe created successfully");
            return "redirect:/recipe";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recipe/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            RecipeDTO recipe = recipeService.getRecipeById(id, tenantId());
            if (recipe.getDetails() == null || recipe.getDetails().isEmpty()) {
                recipe.setDetails(new ArrayList<>());
                recipe.getDetails().add(new RecipeDetailDTO());
            }
            model.addAttribute("recipe", recipe);
            model.addAttribute("ingredients", ingredientService.getPaginatedActiveIngredients(tenantId(), PageRequest.of(0, 1000)));
            return "recipe/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/recipe";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("recipe") RecipeDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ingredients", ingredientService.getPaginatedActiveIngredients(tenantId(), PageRequest.of(0, 1000)));
            return "recipe/form";
        }
        try {
            recipeService.updateExistingRecipe(id, dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Recipe updated successfully");
            return "redirect:/recipe";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/recipe/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            recipeService.toggleRecipeActiveStatus(id, tenantId());
            redirectAttributes.addFlashAttribute("message", "Recipe status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/recipe";
    }

    private String tenantId() {
        return TenantContext.get();
    }
}
