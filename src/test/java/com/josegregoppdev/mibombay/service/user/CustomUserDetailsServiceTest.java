package com.josegregoppdev.mibombay.service.user;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void loadUserByUsername_userExists_returnsUserDetails() {
        User user = createAdmin();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername(user.getEmail());

        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPasswordHash(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_userDoesNotExist_throwsException() {
        String email = "noexist@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(email));
    }

    @Test
    void loadUserByUsername_inactiveUser_throwsException() {
        User user = createAdmin();
        user.setActive(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(user.getEmail()));
    }

    @Test
    void loadUserByUsername_setsTenantIdInContext() {
        User user = createAdmin();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        service.loadUserByUsername(user.getEmail());

        assertEquals(user.getTenantId(), TenantContext.get());
    }

    @Test
    void loadUserByUsername_superAdmin_doesNotSetTenantContext() {
        User superAdmin = createSuperAdmin();
        TenantContext.set("SOME_TENANT");
        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        service.loadUserByUsername(superAdmin.getEmail());

        assertEquals("SOME_TENANT", TenantContext.get());
    }

    @Test
    void loadUserByUsername_tenantMatches_ok() {
        User user = createAdmin();
        TenantContext.set(user.getTenantId());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername(user.getEmail());

        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_tenantDoesNotMatch_throwsException() {
        User user = createAdmin();
        TenantContext.set("tnt_other_tenant00000000000000000000");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(user.getEmail()));

        assertTrue(ex.getMessage().contains("Access denied"));
    }
}
