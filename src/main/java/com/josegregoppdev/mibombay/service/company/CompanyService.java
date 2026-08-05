package com.josegregoppdev.mibombay.service.company;

import com.josegregoppdev.mibombay.dto.company.CompanyDTORequest;
import com.josegregoppdev.mibombay.dto.company.CompanyDTOResponse;
import com.josegregoppdev.mibombay.mapper.company.CompanyMapper;
import com.josegregoppdev.mibombay.model.company.Company;
import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.company.CompanyRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import com.josegregoppdev.mibombay.service.user.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;
    private final CompanyMapper companyMapper;

    @Transactional
    public CompanyDTOResponse register(CompanyDTORequest dto) {
        validateSubdomain(dto.getSubdomain());
        validateManagerEmail(dto.getManagerEmail());
        validatePasswords(dto.getManagerPassword(), dto.getConfirmManagerPassword());

        String tenantId = generateTenantId();

        Company company = companyMapper.toEntity(dto);
        company.setTenantId(tenantId);
        company.setActive(true);
        company.setManagerDocumentHash(passwordEncoder.encode(dto.getManagerDocument()));
        companyRepository.save(company);

        User admin = User.builder()
                .tenantId(tenantId)
                .email(dto.getManagerEmail())
                .passwordHash(passwordEncoder.encode(dto.getManagerPassword()))
                .fullName(dto.getManagerName())
                .phone(dto.getManagerPhone())
                .documentHash(company.getManagerDocumentHash())
                .role(Role.ADMIN)
                .active(true)
                .mustChangePassword(false)
                .lastPasswordChange(LocalDateTime.now())
                .build();
        userRepository.save(admin);

        String cashierPassword = passwordGeneratorService.generateTemporaryPassword();
        String cashierEmail = generateCashierEmail(dto.getManagerEmail());

        User cashier = User.builder()
                .tenantId(tenantId)
                .email(cashierEmail)
                .passwordHash(passwordEncoder.encode(cashierPassword))
                .fullName("Cashier " + dto.getName())
                .documentHash(company.getManagerDocumentHash())
                .role(Role.CASHIER)
                .active(true)
                .mustChangePassword(true)
                .build();
        userRepository.save(cashier);

        return CompanyDTOResponse.builder()
                .companyName(dto.getName())
                .cashierEmail(cashierEmail)
                .cashierPassword(cashierPassword)
                .build();
    }

    @Transactional(readOnly = true)
    public CompanyDTOResponse getCompanyByTenantId(String tenantId) {
        Company company = companyRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        return companyMapper.toResponse(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyDTOResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(companyMapper::toResponse)
                .toList();
    }

    private void validateSubdomain(String subdomain) {
        if (companyRepository.existsBySubdomain(subdomain)) {
            throw new IllegalArgumentException("The subdomain '" + subdomain + "' is already in use");
        }
    }

    private void validateManagerEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("The email '" + email + "' is already registered");
        }
    }

    private void validatePasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("The passwords do not match");
        }
    }

    private String generateTenantId() {
        return "tnt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    private String generateCashierEmail(String adminEmail) {
        int atIndex = adminEmail.indexOf('@');
        if (atIndex == -1) {
            return "cashier_" + adminEmail;
        }
        return adminEmail.substring(0, atIndex) + "_cashier" + adminEmail.substring(atIndex);
    }
}
