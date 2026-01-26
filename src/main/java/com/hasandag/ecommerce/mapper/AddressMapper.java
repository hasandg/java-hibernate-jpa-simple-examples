package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.dto.AddressCreateDTO;
import com.hasandag.ecommerce.dto.AddressResponseDTO;
import com.hasandag.ecommerce.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "customer", ignore = true)
  Address toEntity(AddressCreateDTO dto);

  AddressResponseDTO toResponseDTO(Address address);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "customer", ignore = true)
  void updateEntityFromDTO(AddressCreateDTO dto, @MappingTarget Address address);
}
