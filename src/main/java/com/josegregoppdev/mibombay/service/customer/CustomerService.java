package com.josegregoppdev.mibombay.service.customer;

import com.josegregoppdev.mibombay.dto.customer.CustomerDTO;
import com.josegregoppdev.mibombay.mapper.customer.CustomerMapper;
import com.josegregoppdev.mibombay.model.customer.Customer;
import com.josegregoppdev.mibombay.repository.customer.CustomerRepository;
import com.josegregoppdev.mibombay.service.security.Aes256GcmEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final Aes256GcmEncryptionService encryptionService;

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getPaginatedCustomers(String tenantId, String name, String document, Pageable pageable) {
        String nameParam = (name != null && !name.isBlank()) ? name : null;
        String docParam = (document != null && !document.isBlank()) ? document : null;
        return customerRepository.findByFilters(tenantId, nameParam, docParam, pageable)
                .map(this::toDtoWithMask);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getPaginatedCustomers(String tenantId, Pageable pageable) {
        return getPaginatedCustomers(tenantId, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id, String tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return toDtoWithMask(customer);
    }

    @Transactional
    public CustomerDTO createNewCustomer(CustomerDTO dto, String tenantId) {
        if (dto.getDocument() != null && !dto.getDocument().isBlank()) {
            String docHash = encryptionService.computeLookupHash(dto.getDocument());
            if (customerRepository.existsByTenantIdAndDocumentLookupHash(tenantId, docHash)) {
                throw new IllegalArgumentException("A customer with that document already exists");
            }
        }

        Customer customer = customerMapper.toEntity(dto);
        customer.setTenantId(tenantId);
        customer.setActive(dto.getActive() != null ? dto.getActive() : true);
        customer.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);

        customer.setDocumentEncrypted(encryptionService.encrypt(dto.getDocument()));
        customer.setPhoneEncrypted(encryptionService.encrypt(dto.getPhone()));
        customer.setDocumentLookupHash(encryptionService.computeLookupHash(dto.getDocument()));
        customer.setPhoneLookupHash(encryptionService.computeLookupHash(dto.getPhone()));

        customer = customerRepository.save(customer);
        return toDtoWithMask(customer);
    }

    @Transactional
    public CustomerDTO updateExistingCustomer(Long id, CustomerDTO dto, String tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customer.getIsDefault()) {
            if (dto.getFullName() != null) {
                customer.setFullName(dto.getFullName());
            }
            if (dto.getAddress() != null) {
                customer.setAddress(dto.getAddress());
            }
            return toDtoWithMask(customerRepository.save(customer));
        }

        if (dto.getDocument() != null && !dto.getDocument().isBlank()) {
            String docHash = encryptionService.computeLookupHash(dto.getDocument());
            if (customerRepository.existsByTenantIdAndDocumentLookupHashAndIdNot(tenantId, docHash, id)) {
                throw new IllegalArgumentException("A customer with that document already exists");
            }
            customer.setDocumentEncrypted(encryptionService.encrypt(dto.getDocument()));
            customer.setDocumentLookupHash(docHash);
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            customer.setPhoneEncrypted(encryptionService.encrypt(dto.getPhone()));
            customer.setPhoneLookupHash(encryptionService.computeLookupHash(dto.getPhone()));
        }

        if (dto.getFullName() != null) {
            customer.setFullName(dto.getFullName());
        }
        if (dto.getAddress() != null) {
            customer.setAddress(dto.getAddress());
        }
        if (dto.getActive() != null) {
            customer.setActive(dto.getActive());
        }

        customer = customerRepository.save(customer);
        return toDtoWithMask(customer);
    }

    @Transactional
    public void toggleCustomerActiveStatus(Long id, String tenantId) {
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (customer.getIsDefault()) {
            throw new IllegalArgumentException("The default customer cannot be deactivated");
        }

        customer.setActive(!customer.getActive());
        customerRepository.save(customer);
    }

    @Transactional
    public CustomerDTO ensureDefaultCustomer(String tenantId) {
        Optional<Customer> existing = customerRepository.findByTenantIdAndIsDefaultTrue(tenantId);
        if (existing.isPresent()) {
            return toDtoWithMask(existing.get());
        }

        CustomerDTO defaultCustomer = CustomerDTO.builder()
                .fullName("Consumidor Final")
                .document("99999999")
                .phone("123456789")
                .address("Cucuta")
                .active(true)
                .isDefault(true)
                .build();

        return createNewCustomer(defaultCustomer, tenantId);
    }

    @Transactional(readOnly = true)
    public Customer getDefaultCustomer(String tenantId) {
        return customerRepository.findByTenantIdAndIsDefaultTrue(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Default customer not found for tenant"));
    }

    @Transactional(readOnly = true)
    public Customer getCustomerEntityById(Long id, String tenantId) {
        return customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    private CustomerDTO toDtoWithMask(Customer customer) {
        CustomerDTO dto = customerMapper.toDto(customer);
        dto.setDocument(encryptionService.decrypt(customer.getDocumentEncrypted()));
        dto.setDocumentMasked(maskValue(dto.getDocument()));
        dto.setPhone(encryptionService.decrypt(customer.getPhoneEncrypted()));
        dto.setPhoneMasked(maskValue(dto.getPhone()));
        dto.setActive(customer.getActive());
        dto.setIsDefault(customer.getIsDefault());
        return dto;
    }

    private String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}
