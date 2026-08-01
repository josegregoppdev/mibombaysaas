package com.josegregoppdev.mibombay.controller.company;

import com.josegregoppdev.mibombay.dto.company.CompanyDTORequest;
import com.josegregoppdev.mibombay.dto.company.CompanyDTOResponse;
import com.josegregoppdev.mibombay.service.company.CompanyRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/registration")
@RequiredArgsConstructor
public class CompanyRegistrationController {

    private final CompanyRegistrationService companyRegistrationService;

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("companyRequest", new CompanyDTORequest());
        return "registration";
    }

    @PostMapping
    public String processRegistration(@Valid @ModelAttribute("companyRequest") CompanyDTORequest companyRequest,
                                      BindingResult bindingResult,
                                      Model model) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        try {
            CompanyDTOResponse result = companyRegistrationService.register(companyRequest);
            model.addAttribute("result", result);
            return "registration-success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registration";
        }
    }
}
