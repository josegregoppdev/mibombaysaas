package com.josegregoppdev.mibombay.controller.dashboard;

import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.company.CompanyRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @GetMapping
    public String dashboard(Authentication authentication,
                            @RequestParam(required = false) String passwordChanged,
                            Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (user.getMustChangePassword()) {
            return "redirect:/change-password";
        }

        if (user.getRole() == Role.SUPER_ADMIN) {
            return "redirect:/admin/dashboard";
        }

        var company = companyRepository.findByTenantId(user.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Company not found"));

        model.addAttribute("user", user);
        model.addAttribute("company", company);

        if (passwordChanged != null) {
            model.addAttribute("message", "Password changed successfully");
        }

        return "dashboard";
    }
}
