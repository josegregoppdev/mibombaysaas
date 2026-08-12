package com.josegregoppdev.mibombay.service.supplier;

import com.josegregoppdev.mibombay.dto.supplier.SupplierDTO;
import com.josegregoppdev.mibombay.mapper.supplier.SupplierMapper;
import com.josegregoppdev.mibombay.model.supplier.Supplier;
import com.josegregoppdev.mibombay.repository.supplier.SupplierRepository;
import com.josegregoppdev.mibombay.service.security.Aes256GcmEncryptionService;
import com.josegregoppdev.mibombay.testdata.TestDataFactory;
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
class SupplierServiceTest {

    private static final String TENANT_ID = "tnt_test1234567890123456789012345678";

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private SupplierMapper supplierMapper;
    @Mock
    private Aes256GcmEncryptionService encryptionService;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void createNewSupplier_encryptedAndSavedSuccessfully() {
        SupplierDTO dto = TestDataFactory.createNewSupplierDTO();
        String doc = dto.getDocument();
        String phone = dto.getPhone();
        Supplier entity = TestDataFactory.createSupplier();
        SupplierDTO dtoOut = TestDataFactory.createSupplierDTO();

        when(encryptionService.encrypt(doc)).thenReturn("enc:900000000");
        when(encryptionService.encrypt(phone)).thenReturn("enc:3115550000");
        when(encryptionService.computeLookupHash(doc)).thenReturn("hash-doc");
        when(encryptionService.computeLookupHash(phone)).thenReturn("hash-phone");
        when(supplierRepository.existsByTenantIdAndDocumentLookupHash(TENANT_ID, "hash-doc")).thenReturn(false);
        when(supplierMapper.toEntity(any())).thenReturn(entity);
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(supplierMapper.toDto(any())).thenReturn(dtoOut);
        when(encryptionService.decrypt("enc:900000000")).thenReturn(doc);
        when(encryptionService.decrypt("enc:3115550000")).thenReturn(phone);

        SupplierDTO result = supplierService.createNewSupplier(dto, TENANT_ID);

        assertNotNull(result);
        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(captor.capture());
        assertEquals(TENANT_ID, captor.getValue().getTenantId());
        assertEquals("enc:900000000", captor.getValue().getDocumentEncrypted());
        assertTrue(captor.getValue().getActive());
    }

    @Test
    void createNewSupplier_duplicateDocument_throwsException() {
        SupplierDTO dto = TestDataFactory.createNewSupplierDTO();
        when(encryptionService.computeLookupHash(dto.getDocument())).thenReturn("hash-doc");
        when(supplierRepository.existsByTenantIdAndDocumentLookupHash(TENANT_ID, "hash-doc")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> supplierService.createNewSupplier(dto, TENANT_ID));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void updateExistingSupplier_reEncryptsChangedDocument() {
        Supplier entity = TestDataFactory.createSupplier();
        SupplierDTO dto = TestDataFactory.createSupplierDTO();
        dto.setPhone(null);
        when(supplierRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(entity));
        when(encryptionService.computeLookupHash(dto.getDocument())).thenReturn("hash-doc");
        when(supplierRepository.existsByTenantIdAndDocumentLookupHashAndIdNot(TENANT_ID, "hash-doc", 1L)).thenReturn(false);
        when(encryptionService.encrypt(dto.getDocument())).thenReturn("enc:new-doc");
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(supplierMapper.toDto(any())).thenReturn(dto);
        when(encryptionService.decrypt("enc:new-doc")).thenReturn(dto.getDocument());
        when(encryptionService.decrypt(entity.getPhoneEncrypted())).thenReturn("3115550000");

        SupplierDTO result = supplierService.updateExistingSupplier(1L, dto, TENANT_ID);

        assertNotNull(result);
        assertEquals("enc:new-doc", entity.getDocumentEncrypted());
        assertEquals("hash-doc", entity.getDocumentLookupHash());
        verify(supplierRepository).save(entity);
    }

    @Test
    void updateExistingSupplier_notFound_throwsException() {
        when(supplierRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> supplierService.updateExistingSupplier(99L, TestDataFactory.createSupplierDTO(), TENANT_ID));
    }

    @Test
    void toggleSupplierActiveStatus_flipsActive() {
        Supplier entity = TestDataFactory.createSupplier();
        when(supplierRepository.findByIdAndTenantId(1L, TENANT_ID)).thenReturn(Optional.of(entity));
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        supplierService.toggleSupplierActiveStatus(1L, TENANT_ID);

        assertFalse(entity.getActive());
        verify(supplierRepository).save(entity);
    }

    @Test
    void toggleSupplierActiveStatus_notFound_throwsException() {
        when(supplierRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> supplierService.toggleSupplierActiveStatus(99L, TENANT_ID));
    }

    @Test
    void getPaginatedSuppliers_returnsPageWithMask() {
        Pageable pageable = PageRequest.of(0, 20);
        Supplier entity = TestDataFactory.createSupplier();
        Page<Supplier> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(supplierRepository.findByFilters(eq(TENANT_ID), isNull(), isNull(), eq(pageable))).thenReturn(page);
        when(supplierMapper.toDto(entity)).thenReturn(TestDataFactory.createSupplierDTO());
        when(encryptionService.decrypt(entity.getDocumentEncrypted())).thenReturn("900000000");
        when(encryptionService.decrypt(entity.getPhoneEncrypted())).thenReturn("3115550000");

        Page<SupplierDTO> result = supplierService.getPaginatedSuppliers(TENANT_ID, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("*****0000", result.getContent().get(0).getDocumentMasked());
    }

    @Test
    void getPaginatedSuppliers_filtersByName() {
        Pageable pageable = PageRequest.of(0, 20);
        Supplier entity = TestDataFactory.createSupplier();
        Page<Supplier> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(supplierRepository.findByFilters(eq(TENANT_ID), eq("Distribuciones"), isNull(), eq(pageable))).thenReturn(page);
        when(supplierMapper.toDto(entity)).thenReturn(TestDataFactory.createSupplierDTO());
        when(encryptionService.decrypt(entity.getDocumentEncrypted())).thenReturn("900000000");
        when(encryptionService.decrypt(entity.getPhoneEncrypted())).thenReturn("3115550000");

        Page<SupplierDTO> result = supplierService.getPaginatedSuppliers(TENANT_ID, "Distribuciones", null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getSupplierById_notFound_throwsException() {
        when(supplierRepository.findByIdAndTenantId(99L, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> supplierService.getSupplierById(99L, TENANT_ID));
    }

    @Test
    void updateExistingSupplier_defaultOnlyUpdatesNameAndAddress() {
        Supplier entity = TestDataFactory.createDefaultSupplier();
        SupplierDTO dto = TestDataFactory.createSupplierDTO();
        dto.setName("Proveedor Renombrado");
        dto.setAddress("Nueva Direccion");
        when(supplierRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(entity));
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(supplierMapper.toDto(any())).thenReturn(dto);
        when(encryptionService.decrypt(entity.getDocumentEncrypted())).thenReturn("900001111");
        when(encryptionService.decrypt(entity.getPhoneEncrypted())).thenReturn("3110001111");

        supplierService.updateExistingSupplier(2L, dto, TENANT_ID);

        assertEquals("Proveedor Renombrado", entity.getName());
        assertEquals("Nueva Direccion", entity.getAddress());
        assertEquals("enc:900001111", entity.getDocumentEncrypted());
        assertEquals("enc:3110001111", entity.getPhoneEncrypted());
        verify(supplierRepository, never()).existsByTenantIdAndDocumentLookupHashAndIdNot(any(), any(), any());
    }

    @Test
    void toggleSupplierActiveStatus_defaultSupplier_throwsException() {
        Supplier entity = TestDataFactory.createDefaultSupplier();
        when(supplierRepository.findByIdAndTenantId(2L, TENANT_ID)).thenReturn(Optional.of(entity));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> supplierService.toggleSupplierActiveStatus(2L, TENANT_ID));
        assertTrue(ex.getMessage().contains("default supplier"));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void ensureDefaultSupplier_existing_returnsIt() {
        Supplier entity = TestDataFactory.createDefaultSupplier();
        when(supplierRepository.findByTenantIdAndIsDefaultTrue(TENANT_ID)).thenReturn(Optional.of(entity));
        when(supplierMapper.toDto(entity)).thenReturn(TestDataFactory.createSupplierDTO());
        when(encryptionService.decrypt(entity.getDocumentEncrypted())).thenReturn("900001111");
        when(encryptionService.decrypt(entity.getPhoneEncrypted())).thenReturn("3110001111");

        SupplierDTO result = supplierService.ensureDefaultSupplier(TENANT_ID);

        assertNotNull(result);
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void ensureDefaultSupplier_missing_createsOne() {
        when(supplierRepository.findByTenantIdAndIsDefaultTrue(TENANT_ID)).thenReturn(Optional.empty());
        when(encryptionService.computeLookupHash("900001111")).thenReturn("hash-default-doc");
        when(encryptionService.computeLookupHash("3110001111")).thenReturn("hash-default-phone");
        when(encryptionService.encrypt("900001111")).thenReturn("enc:default-doc");
        when(encryptionService.encrypt("3110001111")).thenReturn("enc:default-phone");
        when(supplierRepository.existsByTenantIdAndDocumentLookupHash(TENANT_ID, "hash-default-doc")).thenReturn(false);
        Supplier entity = TestDataFactory.createDefaultSupplier();
        when(supplierMapper.toEntity(any())).thenReturn(entity);
        when(supplierRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(supplierMapper.toDto(any())).thenReturn(TestDataFactory.createSupplierDTO());
        when(encryptionService.decrypt("enc:default-doc")).thenReturn("900001111");
        when(encryptionService.decrypt("enc:default-phone")).thenReturn("3110001111");

        SupplierDTO result = supplierService.ensureDefaultSupplier(TENANT_ID);

        assertNotNull(result);
        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(captor.capture());
        assertTrue(captor.getValue().getIsDefault());
        assertEquals("Proveedor Principal", captor.getValue().getName());
    }
}