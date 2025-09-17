package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ProductoAprobacionResponseDTO {
    private Integer id;
    private Integer idCreador;
    private String nombre;
    private String nombreUsuario;

    private String codigo;
    private String descripcion;
    private float precio;
    private float precioDigital;
    private Integer categoriaId;
    private List<String> colores;
    private List<ProductoAprobacionArchivoDTO> archivos;
    private byte[] archivo;
    public ProductoAprobacionResponseDTO(Integer id, Integer idCreador,String nombre, String descripcion, float precio,float precioDigital,Integer categoriaId, List<String> colores, List<ProductoAprobacionArchivoDTO> archivos, String codigo, byte[] archivo, String nombreUsuario) {
        this.id=id;
        this.nombre=nombre;
        this.descripcion=descripcion;
        this.precio=precio;
        this.precioDigital=precioDigital;
        this.colores=colores;
        this.archivos=archivos;
        this.idCreador=idCreador;
        this.categoriaId=categoriaId;
        this.codigo=codigo;
        this.archivo=archivo;
        this.nombreUsuario = nombreUsuario;
    }
}
