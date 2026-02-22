package com.hasandag.ecommerce.mapper;

import com.hasandag.ecommerce.exceptions.ErrorResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public final class ErrorResponseMapper {

  private ErrorResponseMapper() {}

  public static ErrorResponse toErrorResponse(HttpStatus status, String message) {
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatus(status.value());
    response.setError(status.getReasonPhrase());
    response.setMessage(message);
    return response;
  }
}
