package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface JpaUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    boolean existsByGmail(String gmail);
    Optional<UsuarioEntity> findByGmail(String gmail);
    @Procedure(name = "actualizarUsuario")
    void actualizarUsuario(
            @Param("p_gmail") String gmail,
            @Param("p_nombre") String nombre,
            @Param("p_apellido") String apellido,
            @Param("p_direccion") String direccion,
            @Param("p_cp") String cp,
            @Param("p_ciudad") String ciudad,
            @Param("p_telefono") String telefono
    );

    @Transactional
    @Modifying
    @Query("UPDATE UsuarioEntity  u SET u.permiso= :permiso WHERE u.id= :id")
    void modificarPermiso(@Param("id") int id, @Param("permiso") int permiso);
    @Query(value = "CALL obtener_colaboradores()", nativeQuery = true)
    List<Object[]> obtenerColaboradoresSP();
    @Query(value = "CALL obtener_colaborador_por_id(:id)", nativeQuery = true)
    Optional<Object[]> obtenerColaboradorPorIdSP(@Param("id") int id);


}
