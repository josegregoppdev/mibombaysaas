package com.josegregoppdev.mibombay.service.movement;

import com.josegregoppdev.mibombay.dto.movement.MovementDTO;
import com.josegregoppdev.mibombay.mapper.movement.MovementMapper;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.movement.Movement;
import com.josegregoppdev.mibombay.model.movement.MovementType;
import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.user.User;
import com.josegregoppdev.mibombay.repository.ingredient.IngredientRepository;
import com.josegregoppdev.mibombay.repository.movement.MovementRepository;
import com.josegregoppdev.mibombay.repository.product.ProductRepository;
import com.josegregoppdev.mibombay.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovementService {

    private final MovementRepository movementRepository;
    private final MovementMapper movementMapper;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<MovementDTO> getMovementsByTenant(String tenantId, Pageable pageable) {
        return enrichPage(movementRepository.findByTenantId(tenantId, pageable));
    }

    @Transactional(readOnly = true)
    public Page<MovementDTO> getMovementsByTenantAndType(String tenantId, MovementType type, Pageable pageable) {
        return enrichPage(movementRepository.findByTenantIdAndType(tenantId, type, pageable));
    }

    @Transactional(readOnly = true)
    public Page<MovementDTO> getMovementsByFilters(String tenantId, MovementType type,
                                                   LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return enrichPage(movementRepository.findByFilters(tenantId, type, from, to, pageable));
    }

    @Transactional(readOnly = true)
    public List<MovementDTO> getHistorialByIngredient(String tenantId, Long ingredientId) {
        return enrichList(movementRepository.findByTenantIdAndIngredientId(tenantId, ingredientId));
    }

    @Transactional(readOnly = true)
    public List<MovementDTO> getHistorialByProduct(String tenantId, Long productId) {
        return enrichList(movementRepository.findByTenantIdAndProductId(tenantId, productId));
    }

    @Transactional(readOnly = true)
    public List<MovementDTO> getMovementsByDateRange(String tenantId, LocalDateTime from, LocalDateTime to) {
        return enrichList(movementRepository.findByTenantIdAndDateBetween(tenantId, from, to));
    }

    @Transactional(readOnly = true)
    public Page<MovementDTO> getMovementsByReference(String tenantId, Long referenceId, Pageable pageable) {
        return enrichPage(movementRepository.findByTenantIdAndReferenceId(tenantId, referenceId, pageable));
    }

    private Page<MovementDTO> enrichPage(Page<Movement> page) {
        if (page.isEmpty()) {
            return page.map(this::toDto);
        }
        String tenantId = page.getContent().get(0).getTenantId();
        Map<Long, String> ingredientNames = loadIngredientNames(tenantId, ingredientIds(page.getContent()));
        Map<Long, String> productNames = loadProductNames(tenantId, productIds(page.getContent()));
        Map<Long, String> userNames = loadUserNames(userIds(page.getContent()));
        return page.map(movement -> enrich(movement, ingredientNames, productNames, userNames));
    }

    private List<MovementDTO> enrichList(List<Movement> movements) {
        if (movements.isEmpty()) {
            return List.of();
        }
        String tenantId = movements.get(0).getTenantId();
        Map<Long, String> ingredientNames = loadIngredientNames(tenantId, ingredientIds(movements));
        Map<Long, String> productNames = loadProductNames(tenantId, productIds(movements));
        Map<Long, String> userNames = loadUserNames(userIds(movements));
        return movements.stream()
                .map(movement -> enrich(movement, ingredientNames, productNames, userNames))
                .toList();
    }

    private MovementDTO enrich(Movement movement, Map<Long, String> ingredientNames,
                               Map<Long, String> productNames, Map<Long, String> userNames) {
        MovementDTO dto = toDto(movement);
        dto.setTypeDisplayName(movement.getType().getDisplayName());
        if (movement.getIngredientId() != null) {
            dto.setIngredientName(ingredientNames.get(movement.getIngredientId()));
        }
        if (movement.getProductId() != null) {
            dto.setProductName(productNames.get(movement.getProductId()));
        }
        if (movement.getUserId() != null) {
            dto.setUserName(userNames.get(movement.getUserId()));
        }
        return dto;
    }

    private MovementDTO toDto(Movement movement) {
        MovementDTO dto = movementMapper.toDto(movement);
        dto.setTypeDisplayName(movement.getType().getDisplayName());
        return dto;
    }

    private Set<Long> ingredientIds(List<Movement> movements) {
        return movements.stream().map(Movement::getIngredientId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<Long> productIds(List<Movement> movements) {
        return movements.stream().map(Movement::getProductId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<Long> userIds(List<Movement> movements) {
        return movements.stream().map(Movement::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Map<Long, String> loadIngredientNames(String tenantId, Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Ingredient> ingredients = ingredientRepository.findByTenantIdAndIdIn(tenantId, ids);
        return ingredients.stream().collect(Collectors.toMap(Ingredient::getId, Ingredient::getName));
    }

    private Map<Long, String> loadProductNames(String tenantId, Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Product> products = productRepository.findByTenantIdAndIdIn(tenantId, ids);
        return products.stream().collect(Collectors.toMap(Product::getId, Product::getName));
    }

    private Map<Long, String> loadUserNames(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<User> users = userRepository.findAllById(ids);
        return users.stream().collect(Collectors.toMap(User::getId, User::getFullName));
    }
}
