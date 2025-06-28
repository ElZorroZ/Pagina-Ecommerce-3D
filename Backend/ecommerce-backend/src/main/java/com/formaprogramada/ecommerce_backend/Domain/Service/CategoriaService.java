package com.formaprogramada.ecommerce_backend.Domain.Service;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CategoriaService {

    Categoria CrearCategoria(Categoria categoria);
    Categoria CrearCategoriaConImagen(Categoria categoria, MultipartFile file)throws IOException;
    List<CategoriaEntity> LeerCategorias(List<CategoriaEntity> lista);
    CategoriaEntity LeerCategoria(Categoria categoria);
    Categoria ModificarCategoria(Categoria categoria, int id);
    void BorrarCategoria(int id);

}
