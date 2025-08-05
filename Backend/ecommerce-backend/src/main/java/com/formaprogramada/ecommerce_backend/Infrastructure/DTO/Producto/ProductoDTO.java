package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer categoriaId;
    private Float precio;
    private Float precioDigital;
    private String archivo;
    private String archivoComprimido; // nuevo campo para archivo comprimido
    private String codigoInicial;
    private String version;
    private String seguimiento;
    private String dimensionAlto;
    private String dimensionAncho;
    private String dimensionProfundidad;
    private String material;
    private String tecnica;
    private String peso;
}
