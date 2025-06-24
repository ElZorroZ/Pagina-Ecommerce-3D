package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ResetPassword;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordConfirmRequest {
    private String token;
    private String nuevaPassword;
}
