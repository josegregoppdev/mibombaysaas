package com.josegregoppdev.mibombay.service.customer;

import com.josegregoppdev.mibombay.dto.customer.CustomerDTO;
import com.josegregoppdev.mibombay.mapper.customer.CustomerMapper;
import com.josegregoppdev.mibombay.model.customer.Customer;
import com.josegregoppdev.mibombay.repository.customer.CustomerRepository;
import com.josegregoppdev.mibombay.service.security.Aes256GcmEncryptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Mock private CustomerRepository customerRepository;
    @Mock private CustomerMapper customerMapper;
    @Mock private Aes256GcmEncryptionService encryptionService;

    @InjectMocks private CustomerService customerService;

    private CustomerDTO buildDto() {
        return CustomerDTO.builder()
                .fullName("Juan Perez")
                .document("12345678")
                .phone("3001234567")
                .address("Carrera 1 # 1-01")
                .build();
    }

    private Customer buildCustomer() {
        return Customer.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .fullName("Juan Perez")
                .documentEncrypted("enc:doc")
                .phoneEncrypted("enc:phone")
                .address("Carrera 1 # 1-01")
                .documentLookupHash("hash-doc")
                .phoneLookupHash("hash-phone")
                .active(true)
                .isDefault(false)
                .build();
    }

    @Test
    void createNewCustomer_encryptedAndSavedSuccessfully() {
        CustomerDTO dto = buildDto();
        Customer customer = buildCustomer();

        when(encryptionService.encrypt("12345678")).thenReturn("enc:doc");
        when(encryptionService.encrypt("3001234567")).thenReturn("enc:phone");
        when(encryptionService.computeLookupHash("12345678")).thenReturn("hash-doc");
        when(encryptionService.computeLookupHash("3001234567")).thenReturn("hash-phone");
        when(customerRepository.existsByTenantIdAndDocumentLookupHash(TENANT_ID, "hash-doc")).thenReturn(false);
        when(customerMapper.toEntity(any())).thenReturn(customer);
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerMapper.toDto(any())).thenReturn(buildDto());
        when(encryptionService.decrypt("enc:doc")).thenReturn("12345678");
        when(encryptionService.decrypt("enc:phone")).thenReturn("3001234567");

        CustomerDTO result = customerService.createNewCustomer(dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("12345678", result.getDocument());
        assertEquals("3001234567", result.getPhone());
        assertEquals("****5678", result.getDocumentMasked());
        assertEquals("******4567", result.getPhoneMasked());
        verify(customerRepository).save(any());
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertEquals(TENANT_ID, captor.getValue().getTenantId());
        assertTrue(captor.getValue().getActive());
    }

    @Test
    void createNewCustomer_duplicateDocument_throwsException() {
        CustomerDTO dto = buildDto();
        when(encryptionService.computeLookupHash("12345678")).thenReturn("hash-doc");
        when(customerRepository.existsByTenantIdAndDocumentLookupHash(TENANT_ID, "hash-doc")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.createNewCustomer(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void toggleCustomerActiveStatus_flipsActive() {
        Customer customer = buildCustomer();
        when(customerRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        customerService.toggleCustomerActiveStatus(1L, TENANT_ID);

        assertFalse(customer.getActive());
        verify(customerRepository).save(customer);
    }

    @Test
    void toggleCustomerActiveStatus_defaultCustomer_throwsException() {
        Customer customer = buildCustomer();
        customer.setIsDefault(true);

        when(customerRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(customer));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> customerService.toggleCustomerActiveStatus(1L, TENANT_ID));
        assertTrue(ex.getMessage().contains("default customer"));
    }

    @Test
    void getPaginatedCustomers_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Customer customer = buildCustomer();
        Page<Customer> page = new PageImpl<>(List.of(customer), pageable, 1);
        when(customerRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), eq(pageable))).thenReturn(page);
        when(customerMapper.toDto(customer)).thenReturn(buildDto());
        when(encryptionService.decrypt("enc:doc")).thenReturn("12345678");
        when(encryptionService.decrypt("enc:phone")).thenReturn("3001234567");

        Page<CustomerDTO> result = customerService.getPaginatedCustomers(TENANT_ID, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getCustomerById_notFound_throwsException() {
        when(customerRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> customerService.getCustomerById(99L, TENANT_ID));
    }
}
