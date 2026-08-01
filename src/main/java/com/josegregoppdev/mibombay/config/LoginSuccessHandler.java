package com.josegregoppdev.mibombay.config;

import com.josegregoppdev.mibombay.model.company.Company;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.company.CompanyRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (user.getMustChangePassword()) {
            response.sendRedirect("/change-password");
            return;
        }

        if (user.getRole() == Role.SUPER_ADMIN) {
            response.sendRedirect("/admin/dashboard");
            return;
        }

        String subdomainParam = request.getParameter("subdomain");
        if (subdomainParam == null || subdomainParam.isBlank()) {
            response.sendRedirect("/login?error=true");
            return;
        }

        Company company = companyRepository.findBySubdomain(subdomainParam)
                .orElse(null);

        if (company == null || !company.getTenantId().equals(user.getTenantId())) {
            response.sendRedirect("/login?error=true");
            return;
        }

        String serverName = request.getServerName();
        String domain;
        if (serverName != null && serverName.split("\\.").length > 2) {
            domain = serverName.substring(serverName.indexOf('.') + 1);
        } else {
            domain = serverName != null ? serverName : "lvh.me";
        }
        int port = request.getServerPort();
        String scheme = request.getScheme();
        String redirect = scheme + "://" + subdomainParam + "." + domain
                + (port == 80 || port == 443 ? "" : ":" + port)
                + "/dashboard";
        response.sendRedirect(redirect);
    }
}
