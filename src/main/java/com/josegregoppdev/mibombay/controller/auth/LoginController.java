package com.josegregoppdev.mibombay.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String expired,
                        Model model) {
        if (expired != null) {
            model.addAttribute("error", "Your session has expired. Please log in again.");
        }
        if (error != null) {
            model.addAttribute("error", "Incorrect email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have logged out successfully");
        }
        return "login";
    }

    @GetMapping("/admin/login")
    public String adminLogin(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String expired,
                             Model model) {
        if (expired != null) {
            model.addAttribute("error", "Your session has expired. Please log in again.");
        }
        if (error != null) {
            model.addAttribute("error", "Incorrect email or password");
        }
        return "admin/login";
    }
}
