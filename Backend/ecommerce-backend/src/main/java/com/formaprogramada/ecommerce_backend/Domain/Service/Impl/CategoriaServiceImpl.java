package com.formaprogramada.ecommerce_backend.Domain.Service.Impl;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaDestacadoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaArchivoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaDestacadoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoArchivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private ImgBBUploaderService imgBBUploaderService;

    private final CategoriaRepository categoriaRepository;

    @Autowired
    private JpaCategoriaArchivoRepository jpaCategoriaArchivoRepository;

    private JpaCategoriaDestacadoRepository JpaCategoriaDestacadoRepository;
    private final JpaCategoriaDestacadoRepository jpaCategoriaDestacadoRepository;

    @Override
    public Categoria CrearCategoria(Categoria categoria) {


        return categoriaRepository.guardar(categoria);
    }

    @Override
    public Categoria CrearCategoriaConImagen(Categoria categoria, MultipartFile file)throws IOException {
        Categoria categoria1=categoriaRepository.guardar(categoria);
        ImgBBData data = imgBBUploaderService.subirImagen(file);



        CategoriaEntity idCategoria=categoriaRepository.LeerUnoSinImagen(categoria);
        if(idCategoria==null) {
            throw new RuntimeException("Producto no encontrado");
        }
        CategoriaArchivoEntity archivo= new CategoriaArchivoEntity();

        archivo.setCategoriaId(idCategoria);
        archivo.setLinkArchivo(data.getUrl());
        archivo.setDeleteUrl(data.getDelete_url());
        jpaCategoriaArchivoRepository.save(archivo);

        return categoria1;
    }

    @Override
    public Map<CategoriaEntity,String> LeerCategorias( Map<CategoriaEntity,String> lista) {

        lista=categoriaRepository.LeerTodo(lista);
        return lista;
    }

    @Override
    public Map<CategoriaEntity,String> LeerCategoria(Categoria categoria) {


        return categoriaRepository.LeerUno(categoria);
    }

    @Override
    public Categoria ModificarCategoria(Categoria categoria, int id) {


        return categoriaRepository.modificar(categoria,id);
    }

    @Override
    public boolean ModificarCategoriaImagen(MultipartFile file, int id) {

        CategoriaArchivoEntity imagen=jpaCategoriaArchivoRepository.findBycategoriaId(id) .orElseThrow(() -> new RuntimeException("No se encontró la imagen"));
        imgBBUploaderService.borrarImagenDeImgBB(imagen.getDeleteUrl());
        jpaCategoriaArchivoRepository.delete(imagen);
        try {
            CategoriaEntity caId= new CategoriaEntity();
            caId.setId(id);
            ImgBBData data = imgBBUploaderService.subirImagen(file);
            CategoriaArchivoEntity archivo= new CategoriaArchivoEntity();

            archivo.setCategoriaId(caId);
            archivo.setLinkArchivo(data.getUrl());
            archivo.setDeleteUrl(data.getDelete_url());

            jpaCategoriaArchivoRepository.save(archivo);
            return true;
        } catch (IOException e) {
            return false;
        }




    }

    @Override
    public void BorrarCategoria(int id) {
        String url=categoriaRepository.borrarImagen(id);
        if (url!=null) {
            imgBBUploaderService.borrarImagenDeImgBB(url);
        }else{
            categoriaRepository.borrar(id);
        }

    }

    @Override
    public boolean AgregarCategoriaDestacada(int id) {
        if(jpaCategoriaDestacadoRepository.findAll().size()<2) {
            Categoria cat = new Categoria();
            cat.setId(id);
            CategoriaEntity cat2 = categoriaRepository.LeerUnoSinImagen(cat);
            return categoriaRepository.AgregarDestacado(cat2);
        }
        else{
            return false;
        }

    }


}
