package com.josegregoppdev.mibombay.controller.product;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.product.ProductDTO;
import com.josegregoppdev.mibombay.model.product.ProductCategory;
import com.josegregoppdev.mibombay.model.product.ProductType;
import com.josegregoppdev.mibombay.service.product.ProductService;
import com.josegregoppdev.mibombay.service.recipe.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final RecipeService recipeService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) ProductCategory category,
                       @RequestParam(required = false) ProductType productType,
                       @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
                       Pageable pageable, Model model) {
        model.addAttribute("page", productService.getPaginatedProducts(
                tenantId(), name, category, productType, pageable));
        model.addAttribute("name", name);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedProductType", productType);
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("productTypes", ProductType.values());
        return "product/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("product", new ProductDTO());
        model.addAttribute("categories", ProductCategory.values());
        model.addAttribute("productTypes", ProductType.values());
        model.addAttribute("recipes", recipeService.getPaginatedRecipes(
                tenantId(), PageRequest.of(0, 1000)));
        return "product/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("product") ProductDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", ProductCategory.values());
            model.addAttribute("productTypes", ProductType.values());
            model.addAttribute("recipes", recipeService.getPaginatedRecipes(
                    tenantId(), PageRequest.of(0, 1000)));
            return "product/form";
        }
        try {
            productService.createNewProduct(dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Product created successfully");
            return "redirect:/product";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("product", productService.getProductById(id, tenantId()));
            model.addAttribute("categories", ProductCategory.values());
            model.addAttribute("productTypes", ProductType.values());
            model.addAttribute("recipes", recipeService.getPaginatedRecipes(
                    tenantId(), PageRequest.of(0, 1000)));
            return "product/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/product";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("product") ProductDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", ProductCategory.values());
            model.addAttribute("productTypes", ProductType.values());
            model.addAttribute("recipes", recipeService.getPaginatedRecipes(
                    tenantId(), PageRequest.of(0, 1000)));
            return "product/form";
        }
        try {
            productService.updateExistingProduct(id, dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Product updated successfully");
            return "redirect:/product";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            productService.toggleProductActiveStatus(id, tenantId());
            redirectAttributes.addFlashAttribute("message", "Product status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/product";
    }

    private String tenantId() {
        return TenantContext.get();
    }
}
