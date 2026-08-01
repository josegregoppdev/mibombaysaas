package com.josegregoppdev.mibombay.controller.company;

import com.josegregoppdev.mibombay.dto.company.CompanyDTORequest;
import com.josegregoppdev.mibombay.dto.company.CompanyDTOResponse;
import com.josegregoppdev.mibombay.service.company.CompanyRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static com.josegregoppdev.mibombay.testdata.TestDataFactory.createCompanyDTOResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyRegistrationControllerTest {

    @Mock
    private CompanyRegistrationService companyRegistrationService;

    @InjectMocks
    private CompanyRegistrationController controller;

    @Test
    void showForm_returnsRegistrationView() {
        Model model = new ExtendedModelMap();
        String view = controller.showForm(model);
        assertEquals("registration", view);
        assertTrue(model.containsAttribute("companyRequest"));
    }

    @Test
    void processRegistration_success_returnsSuccessView() {
        CompanyDTOResponse result = createCompanyDTOResponse();
        when(companyRegistrationService.register(any())).thenReturn(result);

        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = controller.processRegistration(new CompanyDTORequest(), bindingResult, model);
        assertEquals("registration-success", view);
        assertTrue(model.containsAttribute("result"));
    }

    @Test
    void processRegistration_withValidationError_returnsRegistrationView() {
        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.processRegistration(new CompanyDTORequest(), bindingResult, model);
        assertEquals("registration", view);
    }

    @Test
    void processRegistration_withException_returnsRegistrationViewWithError() {
        when(companyRegistrationService.register(any()))
                .thenThrow(new IllegalArgumentException("The subdomain is already in use"));

        Model model = new ExtendedModelMap();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = controller.processRegistration(new CompanyDTORequest(), bindingResult, model);
        assertEquals("registration", view);
        assertTrue(model.containsAttribute("error"));
    }
}
