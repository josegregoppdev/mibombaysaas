package com.josegregoppdev.mibombay.service.sale;

import com.josegregoppdev.mibombay.dto.sale.SaleDTO;
import com.josegregoppdev.mibombay.dto.sale.SaleDetailDTO;
import com.josegregoppdev.mibombay.mapper.sale.SaleMapper;
import com.josegregoppdev.mibombay.model.combo.Combo;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.sale.PaymentMethod;
import com.josegregoppdev.mibombay.model.sale.Sale;
import com.josegregoppdev.mibombay.model.sale.SaleDetail;
import com.josegregoppdev.mibombay.model.sale.SaleState;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.combo.ComboRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.repository.sale.SaleRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final ProductRepository productRepository;
    private final ComboRepository comboRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<SaleDTO> getPaginatedSales(String tenantId, SaleState state, Pageable pageable) {
        return saleRepository.findByFilters(tenantId, state, pageable)
                .map(this::mapToDtoWithDetails);
    }

    @Transactional(readOnly = true)
    public Page<SaleDTO> getPaginatedSales(String tenantId, Pageable pageable) {
        return saleRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToDtoWithDetails);
    }

    @Transactional(readOnly = true)
    public SaleDTO getSaleById(Long id, String tenantId) {
        Sale sale = saleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));
        SaleDTO dto = saleMapper.toDto(sale);
        dto.setDetails(mapDetailsToDto(sale));
        dto.setCashierName(sale.getCashier().getFullName());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<SaleDTO> getOnHoldSales(String tenantId) {
        List<Sale> sales = saleRepository.findByTenantIdAndStateOrderBySaleDateDesc(tenantId, SaleState.EN_ESPERA);
        return sales.stream().map(this::mapToDtoWithDetails).toList();
    }

    @Transactional(readOnly = true)
    public List<SaleDTO> getOnHoldSalesByCashier(String tenantId, Long cashierId) {
        List<Sale> sales = saleRepository.findByTenantIdAndStateAndCashierId(tenantId, SaleState.EN_ESPERA, cashierId);
        return sales.stream().map(this::mapToDtoWithDetails).toList();
    }

    @Transactional
    public SaleDTO createSaleFromCart(List<SaleDetailDTO> cartItems, String tenantId, Long cashierId,
                                      String observations) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an empty sale");
        }

        User cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new IllegalArgumentException("Cashier not found"));

        Sale sale = Sale.builder()
                .tenantId(tenantId)
                .saleDate(LocalDateTime.now())
                .state(SaleState.EN_ESPERA)
                .cashier(cashier)
                .observations(observations)
                .details(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (SaleDetailDTO item : cartItems) {
            SaleDetail detail = new SaleDetail();
            detail.setSale(sale);
            detail.setQuantity(item.getQuantity());
            detail.setNotes(item.getNotes());

            if (item.getProductId() != null) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
                detail.setProductId(product.getId());
                detail.setProductName(product.getName());
                detail.setSalePrice(product.getSellingPrice());
                detail.setUnitCost(product.getUnitCost());
            } else if (item.getComboId() != null) {
                Combo combo = comboRepository.findById(item.getComboId())
                        .orElseThrow(() -> new IllegalArgumentException("Combo not found: " + item.getComboId()));
                detail.setComboId(combo.getId());
                detail.setComboName(combo.getName());
                detail.setSalePrice(combo.getSellingPrice());
                detail.setUnitCost(combo.getTotalCost());
            } else {
                throw new IllegalArgumentException("Each sale item must reference a product or a combo");
            }

            detail.setTotalPrice(detail.getQuantity().multiply(detail.getSalePrice()).setScale(4, RoundingMode.HALF_UP));
            sale.getDetails().add(detail);
            total = total.add(detail.getTotalPrice());
        }

        sale.setTotal(total);
        sale = saleRepository.save(sale);
        return saleMapper.toDto(sale);
    }

    @Transactional
    public SaleDTO confirmSale(Long saleId, PaymentMethod paymentMethod, String tenantId) {
        Sale sale = saleRepository.findByIdAndTenantId(saleId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));

        if (sale.getState() != SaleState.EN_ESPERA) {
            throw new IllegalArgumentException("Only on-hold sales can be confirmed");
        }

        sale.setState(SaleState.CONFIRMADA);
        sale.setPaymentMethod(paymentMethod);
        sale = saleRepository.save(sale);
        return saleMapper.toDto(sale);
    }

    @Transactional
    public void cancelSale(Long saleId, String tenantId) {
        Sale sale = saleRepository.findByIdAndTenantId(saleId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));

        if (sale.getState() == SaleState.CONFIRMADA) {
            throw new IllegalArgumentException("Confirmed sales cannot be cancelled");
        }

        sale.setState(SaleState.ANULADA);
        saleRepository.save(sale);
    }

    @Transactional
    public void deleteOnHoldSale(Long saleId, String tenantId) {
        Sale sale = saleRepository.findByIdAndTenantId(saleId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));

        if (sale.getState() != SaleState.EN_ESPERA) {
            throw new IllegalArgumentException("Only on-hold sales can be deleted");
        }

        saleRepository.delete(sale);
    }

    private SaleDTO mapToDtoWithDetails(Sale sale) {
        SaleDTO dto = saleMapper.toDto(sale);
        dto.setDetails(mapDetailsToDto(sale));
        if (sale.getCashier() != null) {
            dto.setCashierId(sale.getCashier().getId());
            dto.setCashierName(sale.getCashier().getFullName());
        }
        return dto;
    }

    private List<SaleDetailDTO> mapDetailsToDto(Sale sale) {
        List<SaleDetailDTO> details = new ArrayList<>();
        for (SaleDetail detail : sale.getDetails()) {
            SaleDetailDTO detailDto = new SaleDetailDTO();
            detailDto.setId(detail.getId());
            detailDto.setProductId(detail.getProductId());
            detailDto.setProductName(detail.getProductName());
            detailDto.setComboId(detail.getComboId());
            detailDto.setComboName(detail.getComboName());
            detailDto.setQuantity(detail.getQuantity());
            detailDto.setSalePrice(detail.getSalePrice());
            detailDto.setUnitCost(detail.getUnitCost());
            detailDto.setTotalPrice(detail.getTotalPrice());
            detailDto.setNotes(detail.getNotes());
            details.add(detailDto);
        }
        return details;
    }
}
