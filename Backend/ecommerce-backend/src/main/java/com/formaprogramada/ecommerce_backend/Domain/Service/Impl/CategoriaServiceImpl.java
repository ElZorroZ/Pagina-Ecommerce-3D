package com.formaprogramada.ecommerce_backend.Domain.Service.Impl;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaArchivoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoArchivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private ImgBBUploaderService imgBBUploaderService;

    private final CategoriaRepository categoriaRepository;

    @Autowired
    private JpaCategoriaArchivoRepository jpaCategoriaArchivoRepository;

    @Override
    public Categoria CrearCategoria(Categoria categoria) {


        return categoriaRepository.guardar(categoria);
    }

    @Override
    public Categoria CrearCategoriaConImagen(Categoria categoria, MultipartFile file)throws IOException {
        Categoria categoria1=categoriaRepository.guardar(categoria);
        ImgBBData data = imgBBUploaderService.subirImagen(file);



        CategoriaEntity idCategoria=categoriaRepository.LeerUno(categoria);
        if(idCategoria==null) {
            throw new RuntimeException("Producto no encontrado");
        }
        CategoriaArchivoEntity archivo= new CategoriaArchivoEntity();

        archivo.setCategoriaId(idCategoria);
        archivo.setLinkArchivo(data.getUrl());
        jpaCategoriaArchivoRepository.save(archivo);

        return categoria1;
    }

    @Override
    public List<CategoriaEntity> LeerCategorias(List<CategoriaEntity> lista) {

        lista=categoriaRepository.LeerTodo(lista);
        return lista;
    }

    @Override
    public CategoriaEntity LeerCategoria(Categoria categoria) {


        return categoriaRepository.LeerUno(categoria);
    }

    @Override
    public Categoria ModificarCategoria(Categoria categoria, int id) {


        return categoriaRepository.modificar(categoria,id);
    }

    @Override
    public void BorrarCategoria(int id) {
        String url=categoriaRepository.borrarImagen(id);
        imgBBUploaderService.borrarImagenDeImgBB(url);
        categoriaRepository.borrar(id);
    }


}
