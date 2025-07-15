package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;


import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDetalleEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Integer id;
    private String nombre;
    private String descripcion;
    private float precio;
    private Integer categoriaId;
    private List<String> colores;
    private boolean destacado;
    private String codigoInicial;
    private Integer version;
    private String seguimiento;
    private String codigo;

    private String dimension;
    private Integer dimensionAlto;
    private Integer dimensionAncho;
    private Integer dimensionProfundidad;
    private String material;
    private String tecnica;
    private String peso;

    public ProductoResponse(ProductoEntity producto, ProductoDetalleEntity detalle, List<String> colores) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.categoriaId = producto.getCategoriaId() != null ? producto.getCategoriaId().getId() : null;
        this.colores = colores;
        this.destacado = isDestacado();

        this.codigo = producto.getCodigo();

        // Descomponer código: asumimos formato ABC12XY3
        if (codigo != null && codigo.length() >= 4) {
            this.codigoInicial = codigo.substring(0, 3); // Ej: ABC

            // Detectar dónde empieza seguimiento (primera letra después de los números)
            int i = 3;
            StringBuilder ver = new StringBuilder();
            while (i < codigo.length() && Character.isDigit(codigo.charAt(i))) {
                ver.append(codigo.charAt(i));
                i++;
            }
            this.version = ver.length() > 0 ? Integer.parseInt(ver.toString()) : null;
            this.seguimiento = i < codigo.length() ? codigo.substring(i) : "";
        }

        if (detalle != null) {
            this.dimension = detalle.getDimension(); // "6x5x2" por ejemplo
            this.material = detalle.getMaterial();
            this.tecnica = detalle.getTecnica();
            this.peso = detalle.getPeso();

            // Separar alto, ancho, profundidad
            if (dimension != null && dimension.matches("\\d+x\\d+x\\d+")) {
                String[] partes = dimension.split("x");
                this.dimensionAlto = Integer.parseInt(partes[0]);
                this.dimensionAncho = Integer.parseInt(partes[1]);
                this.dimensionProfundidad = Integer.parseInt(partes[2]);
            }
        }
    }

}
