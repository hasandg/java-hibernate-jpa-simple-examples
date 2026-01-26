package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.dto.CustomerCreateDTO;
import com.hasandag.ecommerce.dto.CustomerResponseDTO;
import com.hasandag.ecommerce.dto.CustomerUpdateDTO;
import com.hasandag.ecommerce.entity.Customer;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = AddressMapper.class)
public interface CustomerMapper {

  @Mapping(target = "id", ignore = true)
  Customer toEntity(CustomerCreateDTO dto);

  CustomerResponseDTO toResponseDTO(Customer customer);

  @Mapping(target = "id", ignore = true)
  void updateEntityFromDTO(CustomerUpdateDTO dto, @MappingTarget Customer customer);
}
