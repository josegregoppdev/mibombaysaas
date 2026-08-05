package com.josegregoppdev.mibombay.service.user;

import com.josegregoppdev.mibombay.dto.user.UserDTOResponse;
import com.josegregoppdev.mibombay.mapper.user.UserMapper;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserByEmail_returnsUserDTO() {
        User user = User.builder()
                .id(1L)
                .email("admin@test.com")
                .fullName("Admin User")
                .build();
        UserDTOResponse dto = UserDTOResponse.builder()
                .id(1L)
                .email("admin@test.com")
                .fullName("Admin User")
                .build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(dto);

        UserDTOResponse result = userService.getUserByEmail("admin@test.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin@test.com", result.getEmail());
        assertEquals("Admin User", result.getFullName());
    }

    @Test
    void getUserByEmail_userNotFound_throwsException() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail("missing@test.com"));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void changePassword_updatesUserSuccessfully() {
        User user = User.builder()
                .id(1L)
                .email("admin@test.com")
                .passwordHash("oldHash")
                .mustChangePassword(true)
                .build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHash");

        LocalDateTime before = LocalDateTime.now();
        userService.changePassword("admin@test.com", "newPassword123");
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("newHash", saved.getPasswordHash());
        assertFalse(saved.getMustChangePassword());
        assertNotNull(saved.getLastPasswordChange());
        assertFalse(saved.getLastPasswordChange().isBefore(before));
        assertFalse(saved.getLastPasswordChange().isAfter(after));
        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    void changePassword_userNotFound_throwsException() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("missing@test.com", "newPassword123"));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(userRepository, never()).save(any());
    }
}
