package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDetalleAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ProductoAprobacionResponse {

    private Integer id;
    private UsuarioEntity creadorId;
    private String nombre;
    private String descripcion;
    private float precio;
    private float precioDigital;
    private Integer categoriaId;
    private List<ColorRequest> colores; // <-- Cambiado a ColorRequest
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

    public ProductoAprobacionResponse(ProductoAprobacionEntity producto, ProductoDetalleAprobacionEntity detalle, List<ColorRequest> colores) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.categoriaId = producto.getCategoriaId() != null ? producto.getCategoriaId().getId() : null;
        this.colores = colores;
        this.destacado = isDestacado();
        this.creadorId=producto.getUsuarioId();

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
