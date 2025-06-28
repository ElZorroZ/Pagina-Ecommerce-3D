package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoArchivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductoArchivoServiceImpl implements ProductoArchivoService {

    @Autowired
    private JpaProductoArchivoRepository archivoRepository;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private ImgBBUploaderService imgBBUploaderService;
    @Override
    public ProductoArchivoEntity agregarArchivo(ProductoArchivoEntity archivo) {
        return archivoRepository.save(archivo);
    }
    public ProductoArchivoEntity agregarArchivoConImagen(Integer productoId, MultipartFile file, Integer orden) throws IOException {
        ProductoEntity producto = productoService.obtenerProductoPorId(productoId);
        if(producto == null) {
            throw new RuntimeException("Producto no encontrado");
        }

        ImgBBData data = imgBBUploaderService.subirImagen(file);

        ProductoArchivoEntity archivo = new ProductoArchivoEntity();
        archivo.setProducto(producto);
        archivo.setLinkArchivo(data.getUrl());      // URL pública de la imagen
        archivo.setDeleteUrl(data.getDelete_url());  // URL para borrar la imagen en ImgBB
        archivo.setOrden(orden);

        return archivoRepository.save(archivo);
    }
    @Override
    public List<ProductoArchivoEntity> obtenerArchivosPorProductoId(Integer productoId) {
        return archivoRepository.findByProductoIdOrderByOrdenAsc(productoId);
    }

    @Override
    public void eliminarArchivo(Integer id) {
        archivoRepository.deleteById(id);
    }
}

