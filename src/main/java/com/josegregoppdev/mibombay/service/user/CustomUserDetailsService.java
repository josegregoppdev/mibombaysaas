package com.josegregoppdev.mibombay.service.user;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!user.getActive()) {
            throw new UsernameNotFoundException("Inactive user: " + email);
        }

        if (user.getRole() != Role.SUPER_ADMIN) {
            String tenantFromSubdomain = TenantContext.get();
            if (tenantFromSubdomain != null && !tenantFromSubdomain.equals(user.getTenantId())) {
                throw new UsernameNotFoundException("Access denied to this restaurant");
            }
            TenantContext.set(user.getTenantId());
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
