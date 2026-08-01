package com.josegregoppdev.mibombay.common.tenant;

import com.josegregoppdev.mibombay.repository.company.CompanyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter extends OncePerRequestFilter {

    private final CompanyRepository companyRepository;

    public TenantFilter(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String subdomain = extractSubdomain(request.getServerName());
            if (subdomain != null && !subdomain.isBlank()
                    && !subdomain.equals("www")
                    && !subdomain.equals("admin")) {
                companyRepository.findBySubdomain(subdomain)
                        .ifPresent(company -> TenantContext.set(company.getTenantId()));
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractSubdomain(String serverName) {
        if (serverName == null || serverName.isEmpty()) return null;
        String[] parts = serverName.split("\\.");
        if (parts.length <= 2) return null;
        return parts[0];
    }
}
