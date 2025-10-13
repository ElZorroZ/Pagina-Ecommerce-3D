package com.formaprogramada.ecommerce_backend.Domain.Service.Colaborador;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Colaborador.ColaboradorDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColaboradorServiceImpl implements ColaboradorService {
    private final UsuarioRepository usuarioRepository;
    private final JpaUsuarioRepository jpaUsuarioRepository;
    private final UsuarioMapper usuarioMapper;
    @Autowired
    private CacheManager cacheManager;
    @Lazy
    @Autowired
    private ColaboradorCacheProxyService colaboradorCacheProxyService;
    @Override
    public void alternarPermiso(String gmail) {
        UsuarioEntity entity = jpaUsuarioRepository.findByGmail(gmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + gmail));

        Usuario usuario = usuarioMapper.toDomain(entity);

        int nuevoPermiso;
        if (usuario.getPermiso() == 0) {
            nuevoPermiso = 2;
        } else if (usuario.getPermiso() == 2) {
            nuevoPermiso = 0;
        } else {
            throw new IllegalArgumentException("Permiso inválido (" + usuario.getPermiso() + ")");
        }

        usuarioRepository.modificarPermisoUsuario(usuario.getId(), nuevoPermiso);

        // Actualizar cache manualmente
        Cache cache = cacheManager.getCache("colaboradores");
        if (cache != null) {
            // Obtener lista cacheada actual
            List<ColaboradorDTO> listaCacheada = cache.get("colaboradoresList", List.class);
            if (listaCacheada != null) {
                // Si el nuevo permiso es 2, agregamos o actualizamos el colaborador
                if (nuevoPermiso == 2) {
                    // Verificar si ya existe en la lista
                    boolean existe = false;
                    for (int i = 0; i < listaCacheada.size(); i++) {
                        if (listaCacheada.get(i).getId() == usuario.getId()) {
                            // Actualizar datos si es necesario
                            listaCacheada.set(i, new ColaboradorDTO(usuario.getId(), usuario.getNombre(), usuario.getGmail()));
                            existe = true;
                            break;
                        }
                    }
                    if (!existe) {
                        listaCacheada.add(new ColaboradorDTO(usuario.getId(), usuario.getNombre(), usuario.getGmail()));
                    }
                } else if (nuevoPermiso == 0) {
                    // Si el permiso es 0, eliminar de la lista cacheada
                    listaCacheada.removeIf(c -> c.getId() == usuario.getId());
                }
                // Reemplazar la lista en cache con la lista modificada
                cache.put("colaboradoresList", listaCacheada);
            }
            colaboradorCacheProxyService.precargarColaboradores();
        }
    }
    @Cacheable(value = "colaboradores", key = "'colaboradoresList'")
    @Override
    public List<ColaboradorDTO> obtenerColaboradores() {
        List<Object[]> resultados = jpaUsuarioRepository.obtenerColaboradoresSP();
        return resultados.stream()
                .map(r -> new ColaboradorDTO(
                        ((Number) r[0]).intValue(),
                        (String) r[1],
                        (String) r[2]
                ))
                .collect(Collectors.toList());
    }
}
