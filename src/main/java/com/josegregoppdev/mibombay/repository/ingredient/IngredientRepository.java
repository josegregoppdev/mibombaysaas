package com.josegregoppdev.mibombay.repository.ingredient;

import com.josegregoppdev.mibombay.model.ingredient.Category;
import com.josegregoppdev.mibombay.model.ingredient.Ingredient;
import com.josegregoppdev.mibombay.model.ingredient.UnitOfMeasure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Page<Ingredient> findByTenantId(String tenantId, Pageable pageable);

    Page<Ingredient> findByTenantIdAndActiveTrue(String tenantId, Pageable pageable);

    Optional<Ingredient> findByIdAndTenantId(Long id, String tenantId);

    Optional<Ingredient> findByCodeAndTenantId(String code, String tenantId);

    boolean existsByCodeAndTenantId(String code, String tenantId);

    boolean existsByNameAndTenantId(String name, String tenantId);

    @Query("""
            SELECT i FROM Ingredient i WHERE i.tenantId = :tenantId
            AND (:name IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:category IS NULL OR i.category = :category)
            AND (:unitOfMeasure IS NULL OR i.unitOfMeasure = :unitOfMeasure)
            """)
    Page<Ingredient> findByFilters(@Param("tenantId") String tenantId,
                                    @Param("name") String name,
                                    @Param("category") Category category,
                                    @Param("unitOfMeasure") UnitOfMeasure unitOfMeasure,
                                    Pageable pageable);
}
