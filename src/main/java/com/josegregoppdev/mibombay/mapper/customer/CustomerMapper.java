package com.josegregoppdev.mibombay.mapper.customer;

import com.josegregoppdev.mibombay.dto.customer.CustomerDTO;
import com.josegregoppdev.mibombay.model.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    CustomerDTO toDto(Customer entity);

    Customer toEntity(CustomerDTO dto);
}
