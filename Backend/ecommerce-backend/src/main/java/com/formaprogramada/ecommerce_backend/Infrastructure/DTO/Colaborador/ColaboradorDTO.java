package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Colaborador;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColaboradorDTO {
    private int id;
    private String nombre;
    private String gmail;
}
