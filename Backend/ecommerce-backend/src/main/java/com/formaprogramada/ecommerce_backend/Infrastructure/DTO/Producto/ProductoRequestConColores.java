package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestConColores {

    private Integer id; // para edición, si aplica

    private String nombre;

    private String descripcion;

    private Integer categoriaId;

    private float precio;

    private float precioDigital;

    // Colores
    private List<String> colores;

    // Nuevos campos para código
    private String codigoInicial;  // 3 letras

    private String version;       // solo números

    private String seguimiento;    // letras y números

    // Dimensiones separadas
    private Integer dimensionAlto;

    private Integer dimensionAncho;

    private Integer dimensionProfundidad;

    private String material;

    private String peso;  // string para aceptar decimal

    private String tecnica;

}

