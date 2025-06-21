package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Mapper.CategoriaEntityMapper;

public class CategoriaRepositoryImpl implements CategoriaRepository {
    private final JpaCategoriaRepository jpaRepository;
    private final CategoriaEntityMapper mapper;

    public CategoriaRepositoryImpl(JpaCategoriaRepository jpaRepository, CategoriaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean crearCategoria(Categoria categoria) {

        return null;
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return jpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean guardar(Categoria categoria) {
        CategoriaEntity entity = mapper.toEntity(categoria);
        CategoriaEntity savedEntity = jpaRepository.save(entity);
        return true;

    }

}
