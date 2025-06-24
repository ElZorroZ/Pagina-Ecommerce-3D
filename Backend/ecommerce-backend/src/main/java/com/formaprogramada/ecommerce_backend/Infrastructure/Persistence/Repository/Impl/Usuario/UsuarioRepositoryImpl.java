package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Impl.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final JpaUsuarioRepository jpaRepository;
    private final UsuarioMapper mapper;

    public UsuarioRepositoryImpl(JpaUsuarioRepository jpaRepository, UsuarioMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity); // devuelvo el usuario con id
    }

    @Override
    public boolean existePorGmail(String gmail) {
        return jpaRepository.existsByGmail(gmail);
    }

    @Override
    public Optional<Usuario> buscarPorGmail(String gmail) {
        return jpaRepository.findByGmail(gmail)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

