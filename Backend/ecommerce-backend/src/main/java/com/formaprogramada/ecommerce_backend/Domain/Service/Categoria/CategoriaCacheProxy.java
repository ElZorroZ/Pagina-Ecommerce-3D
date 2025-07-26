package com.formaprogramada.ecommerce_backend.Domain.Service.Categoria;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Categoria.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaArchivoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaDestacadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service

public class CategoriaCacheProxy{

    private final CategoriaService categoriaService;

    public CategoriaCacheProxy(
            @Lazy CategoriaService categoriaService
    ) {
        this.categoriaService = categoriaService;
    }


    // Precargar todas las categorías (lista principal)
    public void precargarCategoriasLista() {
        categoriaService.LeerCategorias(); // esto debería tener @Cacheable
    }

    // Precargar categoría individual
    public void precargarCategoriaIndividual(int id) {
        categoriaService.LeerCategoria(id); // esto ya tiene @Cacheable
    }
    // Precargar combo de categorías
    public void precargarCategoriasCombo() {
        categoriaService.LeerCategoriasCombo(); // esto tiene @Cacheable
    }

}