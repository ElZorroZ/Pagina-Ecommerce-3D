package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;
}
