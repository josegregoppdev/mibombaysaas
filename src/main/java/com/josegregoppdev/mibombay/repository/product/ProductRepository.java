package com.josegregoppdev.mibombay.repository.product;

import com.josegregoppdev.mibombay.model.product.Product;
import com.josegregoppdev.mibombay.model.product.ProductCategory;
import com.josegregoppdev.mibombay.model.product.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByTenantId(String tenantId, Pageable pageable);

    Page<Product> findByTenantIdAndActiveTrue(String tenantId, Pageable pageable);

    Optional<Product> findByIdAndTenantId(Long id, String tenantId);

    Optional<Product> findByCodeAndTenantId(String code, String tenantId);

    boolean existsByCodeAndTenantId(String code, String tenantId);

    boolean existsByNameAndTenantId(String name, String tenantId);

    List<Product> findByRecipeIdAndTenantId(Long recipeId, String tenantId);

    @Query("""
            SELECT p FROM Product p WHERE p.tenantId = :tenantId
            AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:category IS NULL OR p.category = :category)
            AND (:productType IS NULL OR p.productType = :productType)
            """)
    Page<Product> findByFilters(@Param("tenantId") String tenantId,
                                @Param("name") String name,
                                @Param("category") ProductCategory category,
                                @Param("productType") ProductType productType,
                                Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.recipe r
            LEFT JOIN FETCH r.details d
            LEFT JOIN FETCH d.ingredient i
            WHERE p.tenantId = :tenantId AND p.active = true
            """)
    List<Product> findAllActiveWithRecipeAndIngredients(@Param("tenantId") String tenantId);
}
