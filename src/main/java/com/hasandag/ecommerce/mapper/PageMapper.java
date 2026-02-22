package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.dto.PageResponseDTO;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public final class PageMapper {

  private PageMapper() {}

  public static <E, D> PageResponseDTO<D> toPageResponseDTO(Page<E> page, Function<E, D> mapper) {
    List<D> content = page.getContent().stream().map(mapper).toList();
    PageResponseDTO<D> dto = new PageResponseDTO<>();
    dto.setContent(content);
    dto.setPage(page.getNumber());
    dto.setSize(page.getSize());
    dto.setTotalElements(page.getTotalElements());
    dto.setTotalPages(page.getTotalPages());
    dto.setFirst(page.isFirst());
    dto.setLast(page.isLast());
    return dto;
  }
}
