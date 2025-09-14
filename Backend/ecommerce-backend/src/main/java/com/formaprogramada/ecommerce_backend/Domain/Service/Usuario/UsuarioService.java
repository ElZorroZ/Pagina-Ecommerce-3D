package com.formaprogramada.ecommerce_backend.Domain.Service.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CambioPassword.CambioPasswordRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;

import java.util.List;

import java.util.Optional;

public interface UsuarioService {

    Usuario registrarUsuario(Usuario usuario);
    List<Usuario> listarTodos();
    boolean existePorGmail(String gmail);
    Usuario actualizarUsuarioPorGmail(String gmail, UsuarioUpdate usuarioUpdate);
    Usuario actualizarUsuario(Usuario usuario);
    Optional<Usuario> buscarPorGmail(String gmail);
    void cambiarPassword(String gmail, CambioPasswordRequest request);
    Boolean actualizarUsuarioAlComprar(Usuario usuario);
    Boolean modificarPermisoUsuario(int id, int permiso);
    String existePorId(int id);

}
