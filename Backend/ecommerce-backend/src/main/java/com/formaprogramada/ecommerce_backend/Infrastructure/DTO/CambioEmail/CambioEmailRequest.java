package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CambioEmail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEmailRequest {
    private String nuevoEmail;
}
