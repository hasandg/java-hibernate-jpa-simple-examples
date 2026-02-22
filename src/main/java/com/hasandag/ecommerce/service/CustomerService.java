package com.hasandag.ecommerce.service;

import com.hasandag.ecommerce.dto.CustomerCreateDTO;
import com.hasandag.ecommerce.dto.CustomerResponseDTO;
import com.hasandag.ecommerce.dto.CustomerUpdateDTO;
import com.hasandag.ecommerce.dto.FilterDTO;
import com.hasandag.ecommerce.dto.PageResponseDTO;
import com.hasandag.ecommerce.entity.Customer;
import com.hasandag.ecommerce.mapper.CustomerMapper;
import com.hasandag.ecommerce.mapper.PageMapper;
import com.hasandag.ecommerce.repository.CustomerRepository;
import com.hasandag.ecommerce.repository.specification.GenericSpecificationBuilder;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final CustomerMapper customerMapper;

  @Transactional
  public CustomerResponseDTO create(CustomerCreateDTO createDTO) {
    if (customerRepository.findByEmail(createDTO.getEmail()).isPresent()) {
      throw new IllegalArgumentException(
          "Customer with email '" + createDTO.getEmail() + "' already exists");
    }

    Customer customer = customerMapper.toEntity(createDTO);
    Customer savedCustomer = customerRepository.save(customer);
    return customerMapper.toResponseDTO(savedCustomer);
  }

  @Transactional(readOnly = true)
  public CustomerResponseDTO findById(Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
    return customerMapper.toResponseDTO(customer);
  }

  @Transactional(readOnly = true)
  public List<CustomerResponseDTO> findAll() {
    return customerRepository.findAll().stream().map(customerMapper::toResponseDTO).toList();
  }

  @Transactional
  public CustomerResponseDTO update(CustomerUpdateDTO updateDTO) {
    Customer customer =
        customerRepository
            .findById(updateDTO.getId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Customer not found with id: " + updateDTO.getId()));

    if (!customer.getEmail().equals(updateDTO.getEmail())) {
      if (customerRepository.findByEmail(updateDTO.getEmail()).isPresent()) {
        throw new IllegalArgumentException(
            "Customer with email '" + updateDTO.getEmail() + "' already exists");
      }
    }

    customerMapper.updateEntityFromDTO(updateDTO, customer);
    Customer updatedCustomer = customerRepository.save(customer);
    return customerMapper.toResponseDTO(updatedCustomer);
  }

  @Transactional(readOnly = true)
  public PageResponseDTO<CustomerResponseDTO> filter(FilterDTO filterDTO) {
    Specification<Customer> spec =
        GenericSpecificationBuilder.buildSpecification(filterDTO, Customer.class);
    return PageMapper.toPageResponseDTO(
        customerRepository.findAll(spec, PageRequest.of(filterDTO.getPage(), filterDTO.getSize())),
        customerMapper::toResponseDTO);
  }

  @Transactional
  public void delete(List<Long> ids) {
    for (Long id : ids) {
      if (!customerRepository.existsById(id)) {
        throw new EntityNotFoundException("Customer not found with id: " + id);
      }
    }
    customerRepository.deleteAllById(ids);
  }
}
