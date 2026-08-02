package com.josegregoppdev.mibombay.mapper.product;

import com.josegregoppdev.mibombay.dto.product.ProductDTO;
import com.josegregoppdev.mibombay.model.product.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    Product toEntity(ProductDTO dto);

    ProductDTO toDto(Product product);
}
