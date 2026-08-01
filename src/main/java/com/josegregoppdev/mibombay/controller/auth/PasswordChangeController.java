package com.josegregoppdev.mibombay.controller.auth;

import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/change-password")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String showForm() {
        return "change-password";
    }

    @PostMapping
    public String changePassword(@RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "The passwords do not match");
            return "change-password";
        }

        if (newPassword.length() < 8) {
            model.addAttribute("error", "The password must be at least 8 characters");
            return "change-password";
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        user.setLastPasswordChange(LocalDateTime.now());
        userRepository.save(user);

        return "redirect:/dashboard?passwordChanged=true";
    }
}
