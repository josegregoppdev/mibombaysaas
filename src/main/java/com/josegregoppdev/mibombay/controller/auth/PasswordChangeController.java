package com.josegregoppdev.mibombay.controller.auth;

import com.josegregoppdev.mibombay.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/change-password")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final UserService userService;

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

        String email = authentication.getName();
        try {
            userService.changePassword(email, newPassword);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "change-password";
        }

        return "redirect:/dashboard?passwordChanged=true";
    }
}
