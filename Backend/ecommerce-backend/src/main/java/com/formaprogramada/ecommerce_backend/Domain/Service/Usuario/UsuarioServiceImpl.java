package com.formaprogramada.ecommerce_backend.Domain.Service.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CambioPassword.CambioPasswordRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import com.formaprogramada.ecommerce_backend.Security.Hasher.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private JpaUsuarioRepository jpaUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final UsuarioMapper mapper;

    @Override
    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        validarDatos(usuario);

        if (usuarioRepository.existePorGmail(usuario.getGmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese Gmail.");
        }

        String hash = passwordHasher.hash(usuario.getPassword());
        usuario.setPassword(hash);
        usuario.setPermiso(false);

        return usuarioRepository.guardar(usuario);
    }

    private void validarDatos(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (usuario.getGmail() == null || usuario.getGmail().isBlank()) {
            throw new IllegalArgumentException("El Gmail es obligatorio.");
        }

        String pass = usuario.getPassword();
        if (pass == null || pass.length() < 8 || pass.length() > 16) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 16 caracteres.");
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public boolean existePorGmail(String gmail) {
        return usuarioRepository.buscarPorGmail(gmail).isPresent();
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        if (usuario.getId() == 0 || usuarioRepository.buscarPorId(usuario.getId()).isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe para actualizar.");
        }
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    @Override
    public Usuario actualizarUsuarioPorGmail(String gmail, UsuarioUpdate usuarioUpdate) {
        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("El usuario a actualizar no existe"));

        jpaUsuarioRepository.actualizarUsuario(
                gmail,
                usuarioUpdate.getNombre(),
                usuarioUpdate.getApellido(),
                usuarioUpdate.getDireccion(),
                usuarioUpdate.getCp(),
                usuarioUpdate.getCiudad(),
                usuarioUpdate.getTelefono()
        );

        return usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new RuntimeException("Error al obtener el usuario actualizado"));
    }

    @Override
    public Optional<Usuario> buscarPorGmail(String gmail) {
        return jpaUsuarioRepository.findByGmail(gmail)
                .map(mapper::toDomain);
    }

    @Override
    public void cambiarPassword(String gmail, CambioPasswordRequest request) {
        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordHasher.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        if (request.getNuevaPassword() == null || request.getNuevaPassword().length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        usuario.setPassword(passwordHasher.hash(request.getNuevaPassword()));
        usuarioRepository.guardar(usuario);
    }
}
