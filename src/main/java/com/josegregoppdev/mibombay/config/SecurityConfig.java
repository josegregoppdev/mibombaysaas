package com.josegregoppdev.mibombay.config;

import com.josegregoppdev.mibombay.service.usuario.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.http.Cookie;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationSuccessHandler loginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/registro", "/login", "/admin/login", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/cambiar-password").authenticated()
                        .requestMatchers("/admin/**").hasRole("SUPER_ADMINISTRADOR")
                        .requestMatchers("/ingrediente/**").hasAnyRole("ADMIN", "SUPER_ADMINISTRADOR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        .invalidSessionUrl("/login?expired=true")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            String serverName = request.getServerName();

            String baseDomain;
            if (serverName != null && serverName.split("\\.").length > 2) {
                baseDomain = serverName.substring(serverName.indexOf('.') + 1);
            } else {
                baseDomain = serverName != null ? serverName : "lvh.me";
            }

            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath("/");
            cookie.setDomain(baseDomain);
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            Cookie cookieSinDomain = new Cookie("JSESSIONID", null);
            cookieSinDomain.setPath("/");
            cookieSinDomain.setMaxAge(0);
            response.addCookie(cookieSinDomain);

            int port = request.getServerPort();
            String scheme = request.getScheme();
            String redirect = scheme + "://" + baseDomain
                    + (port == 80 || port == 443 ? "" : ":" + port)
                    + "/login?logout=true";
            response.sendRedirect(redirect);
        };
    }
}
