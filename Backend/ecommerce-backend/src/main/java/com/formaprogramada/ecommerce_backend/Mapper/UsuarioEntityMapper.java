package com.formaprogramada.ecommerce_backend.Mapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.UsuarioEntity;

@Mapper(componentModel = "spring")
public interface UsuarioEntityMapper {
    UsuarioEntity toEntity(Usuario usuario);

    Usuario toDomain(UsuarioEntity entity);

}
