package com.formaprogramada.ecommerce_backend.Infrastructure.DTO;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;
}
