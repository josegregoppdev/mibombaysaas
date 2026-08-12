package com.josegregoppdev.mibombay.controller.customer;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.customer.CustomerDTO;
import com.josegregoppdev.mibombay.service.customer.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) String document,
                       @PageableDefault(size = 20, sort = "fullName", direction = Sort.Direction.ASC)
                       Pageable pageable, Model model) {
        model.addAttribute("page", customerService.getPaginatedCustomers(
                tenantId(), name, document, pageable));
        model.addAttribute("name", name);
        model.addAttribute("document", document);
        return "customer/list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("customer", new CustomerDTO());
        return "customer/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("customer") CustomerDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customer/form";
        }

        try {
            customerService.createNewCustomer(dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Customer created successfully");
            return "redirect:/customer";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("customer", customerService.getCustomerById(id, tenantId()));
            return "customer/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/customer";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("customer") CustomerDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customer/form";
        }

        try {
            customerService.updateExistingCustomer(id, dto, tenantId());
            redirectAttributes.addFlashAttribute("message", "Customer updated successfully");
            return "redirect:/customer";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/customer/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            customerService.toggleCustomerActiveStatus(id, tenantId());
            redirectAttributes.addFlashAttribute("message", "Customer status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer";
    }

    private String tenantId() {
        return TenantContext.get();
    }
}
