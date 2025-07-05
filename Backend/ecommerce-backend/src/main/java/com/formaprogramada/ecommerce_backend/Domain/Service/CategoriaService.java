package com.formaprogramada.ecommerce_backend.Domain.Service;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CategoriaService {

    Categoria CrearCategoria(Categoria categoria);
    Categoria CrearCategoriaConImagen(Categoria categoria, MultipartFile file)throws IOException;
    Map<CategoriaEntity,String> LeerCategorias( Map<CategoriaEntity,String> lista);
    Map<CategoriaEntity,String> LeerCategoria(Categoria categoria);
    Categoria ModificarCategoria(Categoria categoria, int id);
    boolean ModificarCategoriaImagen(MultipartFile file , int id);
    void BorrarCategoria(int id);
    boolean AgregarCategoriaDestacada(int id);


}
