package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
public class ProductoAprobacionResponseDTO {
    private Integer id;
    private Integer idCreador;
    private String nombre;
    private String descripcion;
    private float precio;
    private List<String> colores;
    private List<ProductoAprobacionArchivoDTO> archivos;

    public ProductoAprobacionResponseDTO(Integer id, String nombre, String descripcion, float precio, List<String> colores, List<ProductoAprobacionArchivoDTO> archivos) {
        this.id=id;
        this.nombre=nombre;
        this.descripcion=descripcion;
        this.precio=precio;
        this.colores=colores;
        this.archivos=archivos;
    }
}
