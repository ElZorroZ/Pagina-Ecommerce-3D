package com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador;

import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoArchivoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoArchivoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoArchivoAprobadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProductoArchivoColaboradorServiceImpl implements ProductoArchivoColaboradorService {
    @Autowired
    private JpaProductoArchivoAprobadoRepository archivoRepository;
    @Autowired
    private JpaProductoAprobacionRepository productoRepository;
    @Override
    public ProductoArchivoAprobacionEntity agregarArchivo(Integer productoId, MultipartFile file, Integer orden) {
        try {
            ProductoAprobacionEntity producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

            ProductoArchivoAprobacionEntity archivo = new ProductoArchivoAprobacionEntity();
            archivo.setProducto(producto);
            archivo.setArchivoImagen(file.getBytes()); // bytes del archivo
            archivo.setOrden(orden);

            return archivoRepository.save(archivo);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo: " + e.getMessage(), e);
        }
    }


}
