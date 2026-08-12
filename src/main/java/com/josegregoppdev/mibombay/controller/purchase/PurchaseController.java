package com.josegregoppdev.mibombay.controller.purchase;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseCartSubmissionDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDTO;
import com.josegregoppdev.mibombay.dto.purchase.PurchaseDetailDTO;
import com.josegregoppdev.mibombay.service.ingredient.IngredientService;
import com.josegregoppdev.mibombay.service.product.ProductService;
import com.josegregoppdev.mibombay.service.purchase.PurchaseService;
import com.josegregoppdev.mibombay.service.supplier.SupplierService;
import com.josegregoppdev.mibombay.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final SupplierService supplierService;
    private final IngredientService ingredientService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(required = false) String supplierName,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) Boolean active,
                       @PageableDefault(size = 20, sort = "purchaseDate", direction = Sort.Direction.DESC)
                       Pageable pageable, Model model) {
        model.addAttribute("page", purchaseService.getPaginatedPurchases(
                tenantId(), supplierName, from, to, active, pageable));
        model.addAttribute("supplierName", supplierName);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("active", active);
        return "purchase/list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("submission", new PurchaseCartSubmissionDTO());
        model.addAttribute("suppliers", supplierService.getAllActiveSuppliersFlat(tenantId()));
        model.addAttribute("ingredients", ingredientService.getAllActiveIngredientsFlat(tenantId()));
        model.addAttribute("products", productService.getAllActiveSinRecetaProductsFlat(tenantId()));
        return "purchase/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute PurchaseCartSubmissionDTO submission,
                         RedirectAttributes redirectAttributes) {
        try {
            List<PurchaseDetailDTO> cart = submission.getItems();
            if (cart == null || cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "The purchase cart is empty");
                return "redirect:/purchase/new";
            }
            Long userId = getCurrentUserId();
            PurchaseDTO purchase = purchaseService.createPurchaseFromCart(
                    cart, tenantId(), userId, submission.getSupplierId(),
                    submission.getObservations(), submission.getPurchaseDate());
            redirectAttributes.addFlashAttribute("message",
                    "Purchase registered successfully (#" + purchase.getId() + ")");
            return "redirect:/purchase/" + purchase.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/purchase/new";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("purchase", purchaseService.getPurchaseById(id, tenantId()));
            return "purchase/detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/purchase";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            purchaseService.cancelPurchase(id, tenantId(), getCurrentUserId());
            redirectAttributes.addFlashAttribute("message", "Purchase cancelled - stock reverted");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchase/" + id;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return userService.getUserByEmail(auth.getName()).getId();
    }

    private String tenantId() {
        return TenantContext.get();
    }
}