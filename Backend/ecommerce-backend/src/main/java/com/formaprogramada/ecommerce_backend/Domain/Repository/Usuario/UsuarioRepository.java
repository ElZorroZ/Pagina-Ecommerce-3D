package com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);
    List<Usuario> findAll();
    boolean existePorGmail(String gmail);
    Optional<Usuario> buscarPorGmail(String gmail); // Útil para login, validación o recuperación
    Optional<Usuario> buscarPorId(Integer id);
    Boolean actualizarUsuario(Usuario usuario);
    Boolean modificarPermisoUsuario(int id, int permiso);

}
