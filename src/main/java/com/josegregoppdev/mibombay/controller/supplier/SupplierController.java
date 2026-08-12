package com.josegregoppdev.mibombay.controller.supplier;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.supplier.SupplierDTO;
import com.josegregoppdev.mibombay.service.supplier.SupplierService;
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
@RequestMapping("/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) String document,
                       @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
                       Pageable pageable, Model model) {
        model.addAttribute("page", supplierService.getPaginatedSuppliers(
                tenantId(), name, document, pageable));
        model.addAttribute("name", name);
        model.addAttribute("document", document);
        return "supplier/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("supplier", new SupplierDTO());
        return "supplier/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("supplier") SupplierDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "supplier/form";
        }

        try {
            supplierService.createNewSupplier(dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Supplier created successfully");
            return "redirect:/supplier";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/supplier/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("supplier", supplierService.getSupplierById(id, tenantId()));
            return "supplier/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/supplier";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("supplier") SupplierDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "supplier/form";
        }

        try {
            supplierService.updateExistingSupplier(id, dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Supplier updated successfully");
            return "redirect:/supplier";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/supplier/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            supplierService.toggleSupplierActiveStatus(id, tenantId());
            redirectAttributes.addFlashAttribute("message", "Supplier status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/supplier";
    }

    private String tenantId() {
        return TenantContext.get();
    }
}