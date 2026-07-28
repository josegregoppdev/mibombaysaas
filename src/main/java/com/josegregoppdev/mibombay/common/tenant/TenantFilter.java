package com.josegregoppdev.mibombay.common.tenant;

import com.josegregoppdev.mibombay.repository.empresa.EmpresaRepository;
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

    private final EmpresaRepository empresaRepository;

    public TenantFilter(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String subdominio = extraerSubdominio(request.getServerName());
            if (subdominio != null && !subdominio.isBlank()
                    && !subdominio.equals("www")
                    && !subdominio.equals("admin")) {
                empresaRepository.findBySubdominio(subdominio)
                        .ifPresent(empresa -> TenantContext.set(empresa.getTenantId()));
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extraerSubdominio(String serverName) {
        if (serverName == null || serverName.isEmpty()) return null;
        String[] parts = serverName.split("\\.");
        if (parts.length <= 2) return null;
        return parts[0];
    }
}
