package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import lombok.*;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductoAprobacionRequest {

    private Integer id; // para edición, si aplica
    private Integer creadorId;
    private String nombre;
    private String descripcion;
    private Integer categoriaId;
    private float precio;
    private float precioDigital;
    private List<ColorRequest> colores;
    private String codigoInicial;  // 3 letras
    private String version;       // solo números
    private String seguimiento;    // letras y números
    private Integer dimensionAlto;
    private Integer dimensionAncho;
    private Integer dimensionProfundidad;
    private String material;
    private String peso;  // string para aceptar decimal
    private String tecnica;
}
