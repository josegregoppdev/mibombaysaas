package com.josegregoppdev.mibombay.controller.dashboard;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.dto.company.CompanyDTOResponse;
import com.josegregoppdev.mibombay.dto.user.UserDTOResponse;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.service.company.CompanyService;
import com.josegregoppdev.mibombay.service.user.UserService;
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

    private final UserService userService;
    private final CompanyService companyService;

    @GetMapping
    public String dashboard(Authentication authentication,
                            @RequestParam(required = false) String passwordChanged,
                            Model model) {
        String email = authentication.getName();
        UserDTOResponse userDto = userService.getUserByEmail(email);

        if (Boolean.TRUE.equals(userDto.getMustChangePassword())) {
            return "redirect:/change-password";
        }

        if (Role.SUPER_ADMIN.name().equals(userDto.getRole())) {
            return "redirect:/admin/dashboard";
        }

        String tenantId = TenantContext.get();
        CompanyDTOResponse companyDto = companyService.getCompanyByTenantId(tenantId);

        model.addAttribute("user", userDto);
        model.addAttribute("company", companyDto);

        if (passwordChanged != null) {
            model.addAttribute("message", "Password changed successfully");
        }

        return "dashboard";
    }
}
