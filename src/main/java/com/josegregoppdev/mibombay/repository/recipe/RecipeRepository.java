package com.josegregoppdev.mibombay.repository.recipe;

import com.josegregoppdev.mibombay.model.recipe.Recipe;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findByTenantId(String tenantId, Pageable pageable);

    Optional<Recipe> findByIdAndTenantId(Long id, String tenantId);

    boolean existsByCodeAndTenantId(String code, String tenantId);

    boolean existsByNameAndTenantId(String name, String tenantId);

    @Query("""
            SELECT r FROM Recipe r WHERE r.tenantId = :tenantId
            AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Recipe> findByFilters(@Param("tenantId") String tenantId,
                               @Param("name") String name,
                               Pageable pageable);
}
