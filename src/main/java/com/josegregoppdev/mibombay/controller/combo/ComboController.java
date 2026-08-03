package com.josegregoppdev.mibombay.controller.combo;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.combo.ComboDTO;
import com.josegregoppdev.mibombay.dto.combo.ComboDetailDTO;
import com.josegregoppdev.mibombay.service.combo.ComboService;
import com.josegregoppdev.mibombay.service.product.ProductService;
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

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/combo")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;
    private final ProductService productService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
                       Pageable pageable, Model model) {
        model.addAttribute("page", comboService.getPaginatedCombos(tenantId(), name, pageable));
        model.addAttribute("name", name);
        return "combo/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        ComboDTO dto = new ComboDTO();
        List<ComboDetailDTO> details = new ArrayList<>();
        details.add(new ComboDetailDTO());
        dto.setDetails(details);
        model.addAttribute("combo", dto);
        model.addAttribute("products", productService.getPaginatedActiveProducts(tenantId(), PageRequest.of(0, 1000)));
        return "combo/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("combo") ComboDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.getPaginatedActiveProducts(tenantId(), PageRequest.of(0, 1000)));
            return "combo/form";
        }
        try {
            comboService.createNewCombo(dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Combo created successfully");
            return "redirect:/combo";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/combo/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            ComboDTO combo = comboService.getComboById(id, tenantId());
            if (combo.getDetails() == null || combo.getDetails().isEmpty()) {
                combo.setDetails(new ArrayList<>());
                combo.getDetails().add(new ComboDetailDTO());
            }
            model.addAttribute("combo", combo);
            model.addAttribute("products", productService.getPaginatedActiveProducts(tenantId(), PageRequest.of(0, 1000)));
            return "combo/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/combo";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("combo") ComboDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.getPaginatedActiveProducts(tenantId(), PageRequest.of(0, 1000)));
            return "combo/form";
        }
        try {
            comboService.updateExistingCombo(id, dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Combo updated successfully");
            return "redirect:/combo";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/combo/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            comboService.toggleComboActiveStatus(id, tenantId());
            redirectAttributes.addFlashAttribute("message", "Combo status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/combo";
    }

    private String tenantId() {
        return TenantContext.get();
    }
}
