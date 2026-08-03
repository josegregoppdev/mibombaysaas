package com.josegregoppdev.mibombay.service.combo;

import com.josegregoppdev.mibombay.dto.combo.ComboDTO;
import com.josegregoppdev.mibombay.dto.combo.ComboDetailDTO;
import com.josegregoppdev.mibombay.mapper.combo.ComboMapper;
import com.josegregoppdev.mibombay.model.combo.Combo;
import com.josegregoppdev.mibombay.model.combo.ComboDetail;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.repository.combo.ComboRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComboService {

    private final ComboRepository comboRepository;
    private final ComboMapper comboMapper;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ComboDTO> getPaginatedCombos(String tenantId, String name, Pageable pageable) {
        String nameParam = (name != null && !name.isBlank()) ? name : null;
        return comboRepository.findByFilters(tenantId, nameParam, pageable)
                .map(this::mapToDtoWithDetails);
    }

    @Transactional(readOnly = true)
    public Page<ComboDTO> getPaginatedCombos(String tenantId, Pageable pageable) {
        return getPaginatedCombos(tenantId, null, pageable);
    }

    @Transactional(readOnly = true)
    public ComboDTO getComboById(Long id, String tenantId) {
        Combo combo = comboRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Combo not found"));
        ComboDTO dto = comboMapper.toDto(combo);
        dto.setDetails(mapDetailsToDto(combo));
        return dto;
    }

    @Transactional
    public ComboDTO createNewCombo(ComboDTO dto, String tenantId) {
        if (comboRepository.existsByCodeAndTenantId(dto.getCode(), tenantId)) {
            throw new IllegalArgumentException("A combo with that code already exists");
        }
        if (comboRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("A combo with that name already exists");
        }

        Combo combo = comboMapper.toEntity(dto);
        combo.setTenantId(tenantId);
        combo.setActive(true);
        combo.setDetails(new ArrayList<>());

        if (dto.getDetails() != null) {
            for (ComboDetailDTO detailDto : dto.getDetails()) {
                if (detailDto.getProductId() == null || detailDto.getQuantity() == null) {
                    continue;
                }
                Product product = productRepository.findById(detailDto.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + detailDto.getProductId()));

                ComboDetail detail = new ComboDetail();
                detail.setCombo(combo);
                detail.setProduct(product);
                detail.setQuantity(detailDto.getQuantity());
                detail.setUnitCost(product.getUnitCost());
                detail.setTotalCost(detail.getQuantity().multiply(detail.getUnitCost()).setScale(4, RoundingMode.HALF_UP));
                detail.setNotes(detailDto.getNotes());
                combo.getDetails().add(detail);
            }
        }

        combo.setTotalCost(calculateTotalCost(combo.getDetails()));
        combo = comboRepository.save(combo);
        return comboMapper.toDto(combo);
    }

    @Transactional
    public ComboDTO updateExistingCombo(Long id, ComboDTO dto, String tenantId) {
        Combo combo = comboRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Combo not found"));

        if (!combo.getCode().equals(dto.getCode())
                && comboRepository.existsByCodeAndTenantId(dto.getCode(), tenantId)) {
            throw new IllegalArgumentException("A combo with that code already exists");
        }
        if (!combo.getName().equals(dto.getName())
                && comboRepository.existsByNameAndTenantId(dto.getName(), tenantId)) {
            throw new IllegalArgumentException("A combo with that name already exists");
        }

        combo.setCode(dto.getCode());
        combo.setName(dto.getName());
        combo.setDescription(dto.getDescription());
        combo.setSellingPrice(dto.getSellingPrice());

        combo.getDetails().clear();

        if (dto.getDetails() != null) {
            for (ComboDetailDTO detailDto : dto.getDetails()) {
                if (detailDto.getProductId() == null || detailDto.getQuantity() == null) {
                    continue;
                }
                Product product = productRepository.findById(detailDto.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + detailDto.getProductId()));

                ComboDetail detail = new ComboDetail();
                detail.setCombo(combo);
                detail.setProduct(product);
                detail.setQuantity(detailDto.getQuantity());
                detail.setUnitCost(product.getUnitCost());
                detail.setTotalCost(detail.getQuantity().multiply(detail.getUnitCost()).setScale(4, RoundingMode.HALF_UP));
                detail.setNotes(detailDto.getNotes());
                combo.getDetails().add(detail);
            }
        }

        combo.setTotalCost(calculateTotalCost(combo.getDetails()));
        combo = comboRepository.save(combo);
        return comboMapper.toDto(combo);
    }

    @Transactional
    public void toggleComboActiveStatus(Long id, String tenantId) {
        Combo combo = comboRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Combo not found"));
        combo.setActive(!combo.getActive());
        comboRepository.save(combo);
    }

    private BigDecimal calculateTotalCost(List<ComboDetail> details) {
        return details.stream()
                .map(ComboDetail::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ComboDetailDTO> mapDetailsToDto(Combo combo) {
        List<ComboDetailDTO> details = new ArrayList<>();
        for (ComboDetail detail : combo.getDetails()) {
            ComboDetailDTO detailDto = new ComboDetailDTO();
            detailDto.setId(detail.getId());
            detailDto.setProductId(detail.getProduct().getId());
            detailDto.setProductName(detail.getProduct().getName());
            detailDto.setQuantity(detail.getQuantity());
            detailDto.setUnitCost(detail.getUnitCost());
            detailDto.setTotalCost(detail.getTotalCost());
            detailDto.setNotes(detail.getNotes());
            details.add(detailDto);
        }
        return details;
    }

    private ComboDTO mapToDtoWithDetails(Combo combo) {
        ComboDTO dto = comboMapper.toDto(combo);
        dto.setDetails(mapDetailsToDto(combo));
        return dto;
    }
}
