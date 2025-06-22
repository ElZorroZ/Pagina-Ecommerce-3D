package com.formaprogramada.ecommerce_backend.Infrastructure.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioPasswordRequest {
    private String passwordActual;
    private String nuevaPassword;
}
