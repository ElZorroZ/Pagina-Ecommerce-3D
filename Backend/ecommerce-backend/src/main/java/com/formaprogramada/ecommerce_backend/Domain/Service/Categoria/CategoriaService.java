package com.formaprogramada.ecommerce_backend.Domain.Service.Categoria;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaComboDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaDTOconImagen;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaUpdateRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CategoriaService {

    Categoria CrearCategoria(Categoria categoria);
    Categoria CrearCategoriaConImagen(Categoria categoria, MultipartFile file)throws IOException;
    List<CategoriaDTO> LeerCategorias();
    CategoriaDTOconImagen LeerCategoria(int id);
    Categoria ModificarCategoria(Categoria categoria, int id);
    boolean ModificarCategoriaImagen(MultipartFile archivoNuevo, int id) throws IOException;
    void BorrarCategoria(int id);
    void toggleCategoriaDestacada(int id);
    List<CategoriaComboDTO> LeerCategoriasCombo();

}
