package com.josegregoppdev.mibombay.controller.sale;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.sale.SaleDTO;
import com.josegregoppdev.mibombay.dto.sale.SaleDetailDTO;
import com.josegregoppdev.mibombay.model.combo.Combo;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.sale.PaymentMethod;
import com.josegregoppdev.mibombay.model.sale.SaleState;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.combo.ComboRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import com.josegregoppdev.mibombay.service.sale.SaleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final ProductRepository productRepository;
    private final ComboRepository comboRepository;
    private final UserRepository userRepository;

    @GetMapping("/pos")
    public String showPOS(Model model, HttpSession session) {
        List<SaleDetailDTO> cart = getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", calculateCartTotal(cart));
        model.addAttribute("products", productRepository.findByTenantIdAndActiveTrue(tenantId(), PageRequest.of(0, 1000)));
        model.addAttribute("combos", comboRepository.findByTenantId(tenantId(), PageRequest.of(0, 1000)));
        model.addAttribute("onHoldCount", saleService.getOnHoldSales(tenantId()).size());
        return "sale/pos";
    }

    @PostMapping("/pos/add")
    public String addToCart(@RequestParam(required = false) Long productId,
                            @RequestParam(required = false) Long comboId,
                            @RequestParam(defaultValue = "1") BigDecimal quantity,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        List<SaleDetailDTO> cart = getCart(session);

        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            SaleDetailDTO item = SaleDetailDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(quantity)
                    .salePrice(product.getSellingPrice())
                    .unitCost(product.getUnitCost())
                    .totalPrice(quantity.multiply(product.getSellingPrice()).setScale(4, java.math.RoundingMode.HALF_UP))
                    .build();
            cart.add(item);
        } else if (comboId != null) {
            Combo combo = comboRepository.findById(comboId)
                    .orElseThrow(() -> new IllegalArgumentException("Combo not found"));
            SaleDetailDTO item = SaleDetailDTO.builder()
                    .comboId(combo.getId())
                    .comboName(combo.getName())
                    .quantity(quantity)
                    .salePrice(combo.getSellingPrice())
                    .unitCost(combo.getTotalCost())
                    .totalPrice(quantity.multiply(combo.getSellingPrice()).setScale(4, java.math.RoundingMode.HALF_UP))
                    .build();
            cart.add(item);
        } else {
            redirectAttributes.addFlashAttribute("error", "Select a product or combo");
            return "redirect:/sale/pos";
        }

        session.setAttribute("cart", cart);
        return "redirect:/sale/pos";
    }

    @PostMapping("/pos/remove")
    public String removeFromCart(@RequestParam int index, HttpSession session) {
        List<SaleDetailDTO> cart = getCart(session);
        if (index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
        session.setAttribute("cart", cart);
        return "redirect:/sale/pos";
    }

    @PostMapping("/pos/confirm")
    public String confirmSale(@RequestParam PaymentMethod paymentMethod,
                              @RequestParam(required = false) String observations,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            List<SaleDetailDTO> cart = getCart(session);
            Long cashierId = getCurrentUserId();

            SaleDTO sale = saleService.createSaleFromCart(cart, tenantId(), cashierId, observations);
            saleService.confirmSale(sale.getId(), paymentMethod, tenantId());

            session.removeAttribute("cart");
            redirectAttributes.addFlashAttribute("message", "Sale confirmed successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sale/pos";
    }

    @PostMapping("/pos/hold")
    public String holdSale(@RequestParam(required = false) String observations,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            List<SaleDetailDTO> cart = getCart(session);
            Long cashierId = getCurrentUserId();

            saleService.createSaleFromCart(cart, tenantId(), cashierId, observations);

            session.removeAttribute("cart");
            redirectAttributes.addFlashAttribute("message", "Sale put on hold");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sale/pos";
    }

    @PostMapping("/pos/cancel")
    public String cancelCart(HttpSession session) {
        session.removeAttribute("cart");
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
            session.setAttribute("cart", cart);
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

    private List<SaleDetailDTO> getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<SaleDetailDTO> cart = (List<SaleDetailDTO>) session.getAttribute("cart");
        return cart != null ? cart : new ArrayList<>();
    }

    private BigDecimal calculateCartTotal(List<SaleDetailDTO> cart) {
        return cart.stream()
                .map(SaleDetailDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
