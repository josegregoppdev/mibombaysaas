package com.josegregoppdev.mibombay.controller.sale;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.sale.CartSubmissionDTO;
import com.josegregoppdev.mibombay.dto.sale.SaleDTO;
import com.josegregoppdev.mibombay.dto.sale.SaleDetailDTO;
import com.josegregoppdev.mibombay.model.sale.PaymentMethod;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import com.josegregoppdev.mibombay.service.combo.ComboService;
import com.josegregoppdev.mibombay.service.product.ProductService;
import com.josegregoppdev.mibombay.service.sale.SaleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ComboService comboService;

    @GetMapping("/pos")
    public String showPOS(Model model, HttpSession session) {
        model.addAttribute("products", productService.getPosProducts(tenantId()));
        model.addAttribute("combos", comboService.getPosCombos(tenantId()));
        model.addAttribute("onHoldCount", saleService.getOnHoldSales(tenantId()).size());
        model.addAttribute("recipeData", productService.getRecipeDataForPos(tenantId()));

        List<SaleDetailDTO> prefill = getPrefillCart(session);
        if (!prefill.isEmpty()) {
            model.addAttribute("prefillCart", prefill);
            session.removeAttribute("prefillCart");
        }

        return "sale/pos";
    }

    @PostMapping("/pos/confirm")
    public String confirmSale(@ModelAttribute CartSubmissionDTO submission,
                              RedirectAttributes redirectAttributes) {
        try {
            List<SaleDetailDTO> cart = submission.getItems();
            if (cart == null || cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cart is empty");
                return "redirect:/sale/pos";
            }

            Long cashierId = getCurrentUserId();
            SaleDTO sale = saleService.createSaleFromCart(cart, tenantId(), cashierId, submission.getObservations());
            saleService.confirmSale(sale.getId(), submission.getPaymentMethod(), tenantId());

            redirectAttributes.addFlashAttribute("message", "Sale confirmed successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sale/pos";
    }

    @PostMapping("/pos/hold")
    public String holdSale(@ModelAttribute CartSubmissionDTO submission,
                           RedirectAttributes redirectAttributes) {
        try {
            List<SaleDetailDTO> cart = submission.getItems();
            if (cart == null || cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cart is empty");
                return "redirect:/sale/pos";
            }

            Long cashierId = getCurrentUserId();
            saleService.createSaleFromCart(cart, tenantId(), cashierId, submission.getObservations());

            redirectAttributes.addFlashAttribute("message", "Sale put on hold");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sale/pos";
    }

    @GetMapping("/on-hold")
    public String listOnHold(Model model) {
        Long cashierId = getCurrentUserId();
        boolean isAdmin = isAdmin();
        if (isAdmin) {
            model.addAttribute("sales", saleService.getOnHoldSales(tenantId()));
        } else {
            model.addAttribute("sales", saleService.getOnHoldSalesByCashier(tenantId(), cashierId));
        }
        return "sale/on-hold";
    }

    @PostMapping("/on-hold/{id}/resume")
    public String resumeSale(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            SaleDTO sale = saleService.getSaleById(id, tenantId());
            List<SaleDetailDTO> cart = new ArrayList<>(sale.getDetails());
            session.setAttribute("prefillCart", cart);
            saleService.cancelSale(id, tenantId());
            return "redirect:/sale/pos";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/sale/on-hold";
        }
    }

    @PostMapping("/on-hold/{id}/delete")
    public String deleteOnHold(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            saleService.deleteOnHoldSale(id, tenantId());
            redirectAttributes.addFlashAttribute("message", "On-hold sale deleted");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sale/on-hold";
    }

    @GetMapping("/history")
    public String listHistory(@PageableDefault(size = 20, sort = "saleDate", direction = Sort.Direction.DESC)
                              Pageable pageable, Model model) {
        model.addAttribute("page", saleService.getPaginatedSales(tenantId(), pageable));
        return "sale/history";
    }

    @GetMapping("/history/{id}")
    public String viewSale(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("sale", saleService.getSaleById(id, tenantId()));
            return "sale/detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/sale/history";
        }
    }

    @SuppressWarnings("unchecked")
    private List<SaleDetailDTO> getPrefillCart(HttpSession session) {
        Object attr = session.getAttribute("prefillCart");
        if (attr instanceof List<?>) {
            return (List<SaleDetailDTO>) attr;
        }
        return new ArrayList<>();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Not authenticated");
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getId();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private String tenantId() {
        return TenantContext.get();
    }
}