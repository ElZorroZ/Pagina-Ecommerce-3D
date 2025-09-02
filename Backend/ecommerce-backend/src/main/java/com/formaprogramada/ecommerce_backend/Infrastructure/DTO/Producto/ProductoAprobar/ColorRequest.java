package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColorRequest {
    private int id;      // ← colorId
    private String nombre;  // nombre opcional
    private String hex;     // color en formato #RRGGBB o #RRGGBBAA
    public ColorRequest(String nombre, String hex) {
        this.nombre = nombre;
        this.hex = hex;
    }

}
