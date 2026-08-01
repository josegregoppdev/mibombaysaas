package com.josegregoppdev.mibombay.controller.admin;

import com.josegregoppdev.mibombay.model.company.Company;
import com.josegregoppdev.mibombay.repository.company.CompanyRepository;
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

    private final CompanyRepository companyRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Company> companies = companyRepository.findAll();
        long total = companies.size();
        long activeCount = companies.stream().filter(Company::getActive).count();
        long inactiveCount = total - activeCount;

        model.addAttribute("total", total);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        return "admin/dashboard";
    }

    @GetMapping("/companies")
    public String companies(Model model) {
        List<Company> companies = companyRepository.findAll();
        model.addAttribute("companies", companies);
        return "admin/companies";
    }
}
