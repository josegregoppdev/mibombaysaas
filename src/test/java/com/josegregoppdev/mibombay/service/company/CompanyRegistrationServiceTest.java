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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class CompanyRegistrationServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordGeneratorService passwordGeneratorService;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyRegistrationService service;

    @Test
    void register_createsAdminAndCashierSuccessfully() {
        CompanyDTORequest dto = createCompanyDTORequest();
        Company company = createCompany();

        when(companyRepository.existsBySubdomain(dto.getSubdomain())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getManagerEmail())).thenReturn(false);
        when(companyMapper.toEntity(dto)).thenReturn(company);
        when(companyRepository.save(any())).thenReturn(company);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
        when(passwordGeneratorService.generateTemporaryPassword()).thenReturn("TempPass123!");
        when(userRepository.save(any())).thenReturn(null);

        CompanyDTOResponse result = service.register(dto);

        assertNotNull(result);
        assertEquals(dto.getName(), result.getCompanyName());
        assertEquals("admin_cashier@test.com", result.getCashierEmail());
        assertEquals("TempPass123!", result.getCashierPassword());
    }

    @Test
    void register_duplicateSubdomain_throwsException() {
        CompanyDTORequest dto = createCompanyDTORequest();

        when(companyRepository.existsBySubdomain(dto.getSubdomain())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(dto));
        assertTrue(ex.getMessage().contains("already in use"));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        CompanyDTORequest dto = createCompanyDTORequest();

        when(companyRepository.existsBySubdomain(dto.getSubdomain())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getManagerEmail())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(dto));
        assertTrue(ex.getMessage().contains("already registered"));
    }

    @Test
    void register_passwordsDoNotMatch_throwsException() {
        CompanyDTORequest dto = createCompanyDTORequest();
        dto.setConfirmManagerPassword("OtherPass456!");

        when(companyRepository.existsBySubdomain(dto.getSubdomain())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getManagerEmail())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(dto));
        assertTrue(ex.getMessage().contains("do not match"));
    }

    @Test
    void register_generatesTenantIdWithCorrectFormat() {
        CompanyDTORequest dto = createCompanyDTORequest();
        Company company = createCompany();

        when(companyRepository.existsBySubdomain(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(companyMapper.toEntity(any())).thenReturn(company);
        when(companyRepository.save(any())).thenReturn(company);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
        when(passwordGeneratorService.generateTemporaryPassword()).thenReturn("TempPass123!");

        service.register(dto);

        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(captor.capture());
        String tenantId = captor.getValue().getTenantId();

        assertNotNull(tenantId);
        assertTrue(tenantId.startsWith("tnt_"));
        assertEquals(36, tenantId.length());
    }

    @Test
    void register_generatesCashierEmailCorrectly() {
        CompanyDTORequest dto = createCompanyDTORequest();
        Company company = createCompany();

        when(companyRepository.existsBySubdomain(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(companyMapper.toEntity(any())).thenReturn(company);
        when(companyRepository.save(any())).thenReturn(company);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
        when(passwordGeneratorService.generateTemporaryPassword()).thenReturn("TempPass123!");

        service.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(captor.capture());
        User cashier = captor.getAllValues().get(1);

        assertNotNull(cashier);
        assertEquals("admin_cashier@test.com", cashier.getEmail());
        assertEquals(Role.CASHIER, cashier.getRole());
        assertTrue(cashier.getMustChangePassword());
    }

    @Test
    void register_hashesManagerDocument() {
        CompanyDTORequest dto = createCompanyDTORequest();
        Company company = createCompany();

        when(companyRepository.existsBySubdomain(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(companyMapper.toEntity(any())).thenReturn(company);
        when(companyRepository.save(any())).thenReturn(company);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$hash");
        when(passwordGeneratorService.generateTemporaryPassword()).thenReturn("TempPass123!");

        service.register(dto);

        verify(passwordEncoder).encode(dto.getManagerDocument());
    }
}
