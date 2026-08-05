package com.josegregoppdev.mibombay.controller.admin;

import com.josegregoppdev.mibombay.dto.company.CompanyDTOResponse;
import com.josegregoppdev.mibombay.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CompanyService companyService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<CompanyDTOResponse> companies = companyService.getAllCompanies();
        long total = companies.size();
        long activeCount = companies.stream().filter(c -> Boolean.TRUE.equals(c.getActive())).count();
        long inactiveCount = total - activeCount;

        model.addAttribute("total", total);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        return "admin/dashboard";
    }

    @GetMapping("/companies")
    public String companies(Model model) {
        List<CompanyDTOResponse> companies = companyService.getAllCompanies();
        model.addAttribute("companies", companies);
        return "admin/companies";
    }
}
