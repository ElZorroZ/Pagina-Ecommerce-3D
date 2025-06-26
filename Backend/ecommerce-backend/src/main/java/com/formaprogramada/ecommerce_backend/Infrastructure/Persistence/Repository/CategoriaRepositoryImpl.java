package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Mapper.CategoriaEntityMapper;
import org.springframework.stereotype.Repository;


@Repository
public class CategoriaRepositoryImpl implements CategoriaRepository {
    private final JpaCategoriaRepository jpaRepository;
    private final CategoriaEntityMapper mapper;


    public CategoriaRepositoryImpl(JpaCategoriaRepository jpaRepository, CategoriaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        CategoriaEntity entity = mapper.toEntity(categoria);
        CategoriaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
