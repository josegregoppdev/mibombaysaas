package com.josegregoppdev.mibombay.service.supplier;

import com.josegregoppdev.mibombay.dto.supplier.SupplierDTO;
import com.josegregoppdev.mibombay.mapper.supplier.SupplierMapper;
import com.josegregoppdev.mibombay.model.supplier.Supplier;
import com.josegregoppdev.mibombay.repository.supplier.SupplierRepository;
import com.josegregoppdev.mibombay.service.security.Aes256GcmEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final Aes256GcmEncryptionService encryptionService;

    @Transactional(readOnly = true)
    public Page<SupplierDTO> getPaginatedSuppliers(String tenantId, String name, String document, Pageable pageable) {
        String nameParam = (name != null && !name.isBlank()) ? name : null;
        String docParam = (document != null && !document.isBlank()) ? document : null;
        return supplierRepository.findByFilters(tenantId, nameParam, docParam, pageable)
                .map(this::toDtoWithMask);
    }

    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(Long id, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
        return toDtoWithMask(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierDTO> getAllActiveSuppliersFlat(String tenantId) {
        return supplierRepository.findByTenantIdAndActiveTrueOrderByNameAsc(tenantId)
                .stream().map(this::toDtoWithMask).toList();
    }

    @Transactional
    public SupplierDTO createNewSupplier(SupplierDTO dto, String tenantId) {
        String docHash = encryptionService.computeLookupHash(dto.getDocument());
        if (supplierRepository.existsByTenantIdAndDocumentLookupHash(tenantId, docHash)) {
            throw new IllegalArgumentException("A supplier with that document already exists");
        }

        Supplier supplier = supplierMapper.toEntity(dto);
        supplier.setTenantId(tenantId);
        supplier.setActive(dto.getActive() != null ? dto.getActive() : true);
        supplier.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);

        supplier.setDocumentEncrypted(encryptionService.encrypt(dto.getDocument()));
        supplier.setDocumentLookupHash(docHash);
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            supplier.setPhoneEncrypted(encryptionService.encrypt(dto.getPhone()));
            supplier.setPhoneLookupHash(encryptionService.computeLookupHash(dto.getPhone()));
        }

        supplier = supplierRepository.save(supplier);
        return toDtoWithMask(supplier);
    }

    @Transactional
    public SupplierDTO updateExistingSupplier(Long id, SupplierDTO dto, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        if (Boolean.TRUE.equals(supplier.getIsDefault())) {
            if (dto.getName() != null) {
                supplier.setName(dto.getName());
            }
            if (dto.getAddress() != null) {
                supplier.setAddress(dto.getAddress());
            }
            supplier = supplierRepository.save(supplier);
            return toDtoWithMask(supplier);
        }

        if (dto.getDocument() != null && !dto.getDocument().isBlank()) {
            String docHash = encryptionService.computeLookupHash(dto.getDocument());
            if (supplierRepository.existsByTenantIdAndDocumentLookupHashAndIdNot(tenantId, docHash, id)) {
                throw new IllegalArgumentException("A supplier with that document already exists");
            }
            supplier.setDocumentEncrypted(encryptionService.encrypt(dto.getDocument()));
            supplier.setDocumentLookupHash(docHash);
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            supplier.setPhoneEncrypted(encryptionService.encrypt(dto.getPhone()));
            supplier.setPhoneLookupHash(encryptionService.computeLookupHash(dto.getPhone()));
        }

        if (dto.getName() != null) {
            supplier.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            supplier.setEmail(dto.getEmail());
        }
        if (dto.getAddress() != null) {
            supplier.setAddress(dto.getAddress());
        }
        if (dto.getContactName() != null) {
            supplier.setContactName(dto.getContactName());
        }
        if (dto.getActive() != null) {
            supplier.setActive(dto.getActive());
        }

        supplier = supplierRepository.save(supplier);
        return toDtoWithMask(supplier);
    }

    @Transactional
    public void toggleSupplierActiveStatus(Long id, String tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        if (Boolean.TRUE.equals(supplier.getIsDefault())) {
            throw new IllegalArgumentException("The default supplier cannot be deactivated");
        }

        supplier.setActive(!supplier.getActive());
        supplierRepository.save(supplier);
    }

    @Transactional
    public SupplierDTO ensureDefaultSupplier(String tenantId) {
        Optional<Supplier> existing = supplierRepository.findByTenantIdAndIsDefaultTrue(tenantId);
        if (existing.isPresent()) {
            return toDtoWithMask(existing.get());
        }

        SupplierDTO defaultSupplier = SupplierDTO.builder()
                .name("Proveedor Principal")
                .document("900001111")
                .phone("3110001111")
                .email("proveedor@mibombay.com")
                .address("Cúcuta")
                .active(true)
                .isDefault(true)
                .build();

        return createNewSupplier(defaultSupplier, tenantId);
    }

    private SupplierDTO toDtoWithMask(Supplier supplier) {
        SupplierDTO dto = supplierMapper.toDto(supplier);
        dto.setDocument(encryptionService.decrypt(supplier.getDocumentEncrypted()));
        dto.setDocumentMasked(maskValue(dto.getDocument()));
        dto.setPhone(encryptionService.decrypt(supplier.getPhoneEncrypted()));
        dto.setPhoneMasked(maskValue(dto.getPhone()));
        dto.setActive(supplier.getActive());
        dto.setIsDefault(supplier.getIsDefault());
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