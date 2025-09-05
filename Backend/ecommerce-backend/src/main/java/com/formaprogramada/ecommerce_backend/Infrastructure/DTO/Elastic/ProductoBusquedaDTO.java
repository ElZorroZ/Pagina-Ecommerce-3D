package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Elastic;

import lombok.*;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductoBusquedaDTO {
    private String query; // Búsqueda general
    private String nombre;
    private Float precioMin;
    private Float precioMax;
    private String ordenarPor; // precio_asc, precio_desc, nombre_asc, nombre_desc
}
