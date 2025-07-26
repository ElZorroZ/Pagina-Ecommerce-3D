package com.formaprogramada.ecommerce_backend.Domain.Service.Categoria;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Categoria.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.MaxDestacadosException;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaComboDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaDTOconImagen;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaDestacadoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaArchivoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaDestacadoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Categoria.CategoriaEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private CategoriaEntityMapper categoriaEntityMapper;
    @Autowired
    private final CategoriaRepository categoriaRepository;
    @Autowired
    private final JpaCategoriaRepository jpaCategoriaRepository;
    @Autowired
    private JpaCategoriaArchivoRepository jpaCategoriaArchivoRepository;
    @Autowired
    private final JpaCategoriaDestacadoRepository jpaCategoriaDestacadoRepository;
    private JpaCategoriaDestacadoRepository JpaCategoriaDestacadoRepository;
    @Autowired
    private CacheManager cacheManager;

    // Crear categoría
    public Categoria CrearCategoria(Categoria categoria) {
        System.out.println("⚠️ Creando categoria");
        if (categoriaRepository.buscarPorNombreIgnoreCase(categoria.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        Categoria creada = categoriaRepository.guardar(categoria);
        cachearCategoriaSinImagen(creada.getId());
        return creada;
    }

    private void cachearCategoriaSinImagen(int categoriaId) {
        System.out.println("⚠️ Consultando base de datos: cachearCategoria");

        CategoriaEntity entidad = jpaCategoriaRepository.LeerUnoSinImagenPorId(categoriaId);
        if (entidad == null) {
            throw new IllegalArgumentException("No se encontró la categoría con ID: " + categoriaId);
        }

        boolean esDestacada = jpaCategoriaDestacadoRepository.findAll().stream()
                .anyMatch(dest -> dest.getCategoria().getId().equals(entidad.getId()));

        CategoriaDTOconImagen dtoConImagen = CategoriaDTOconImagen.builder()
                .id(entidad.getId())
                .nombre(entidad.getNombre())
                .descripcion(entidad.getDescripcion())
                .destacada(esDestacada)
                .linkArchivo(null)
                .build();

        Optional.ofNullable(cacheManager.getCache("categoria"))
                .ifPresent(cache -> cache.put(entidad.getId(), dtoConImagen));

        Optional.ofNullable(cacheManager.getCache("categorias"))
                .ifPresent(cache -> {
                    Object listaObj = cache.get("lista", Object.class);
                    List<CategoriaDTO> lista;

                    if (listaObj instanceof List<?> original) {
                        lista = new ArrayList<>((List<CategoriaDTO>) original);
                        lista.removeIf(c -> c.getId() == entidad.getId());
                    } else {
                        lista = new ArrayList<>();
                    }

                    lista.add(new CategoriaDTO(
                            entidad.getId(),
                            entidad.getNombre(),
                            entidad.getDescripcion(),
                            esDestacada
                    ));

                    cache.put("lista", lista);
                });

        Optional.ofNullable(cacheManager.getCache("categoriasCombo"))
                .ifPresent(cache -> {
                    Object comboObj = cache.get("combo", Object.class);
                    List<CategoriaComboDTO> comboList;

                    if (comboObj instanceof List<?> original) {
                        comboList = new ArrayList<>((List<CategoriaComboDTO>) original);
                        comboList.removeIf(c -> c.getId() == entidad.getId());
                    } else {
                        comboList = new ArrayList<>();
                    }

                    comboList.add(new CategoriaComboDTO(
                            entidad.getId(),
                            entidad.getNombre(),
                            esDestacada,
                            null
                    ));

                    cache.put("combo", comboList);
                });
    }



    @Caching(evict = {
        @CacheEvict(value = "categorias", allEntries = true),
        @CacheEvict(value = "categoriasCombo", allEntries = true)
        }
    )
    @Override
    public Categoria CrearCategoriaConImagen(Categoria categoria, MultipartFile file)throws IOException {
        if (categoriaRepository.buscarPorNombreIgnoreCase(categoria.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria1=categoriaRepository.guardar(categoria);
        ImgBBData data = imgBBUploaderService.subirImagen(file);

        CategoriaArchivoEntity archivo = new CategoriaArchivoEntity();

        CategoriaEntity categoriaEntity = categoriaEntityMapper.toEntity(categoria1); // <- Este es el mapper correcto
        archivo.setCategoriaId(categoriaEntity); // <- Ahora sí tiene ID
        archivo.setLinkArchivo(data.getUrl());
        archivo.setDeleteUrl(data.getDelete_url());
        jpaCategoriaArchivoRepository.save(archivo);

        return categoria1;
    }

    // Cargar lista de categorías (lectura rápida)
    @Cacheable(value = "categorias", key = "'lista'")
    @Override
    public List<CategoriaDTO> LeerCategorias() {
        System.out.println("⚠️ Leer categorias");
        List<CategoriaEntity> categorias = jpaCategoriaRepository.findAll();
        List<CategoriaDestacadoEntity> destacados = jpaCategoriaDestacadoRepository.findAll();

        Set<Integer> idsDestacados = destacados.stream()
                .map(d -> d.getCategoria().getId())
                .collect(Collectors.toSet());

        return categorias.stream()
                .map(cat -> new CategoriaDTO(
                        cat.getId(),
                        cat.getNombre(),
                        cat.getDescripcion(),
                        idsDestacados.contains(cat.getId())
                ))
                .collect(Collectors.toList());
    }

    // Cargar una categoría individual
    @Cacheable(value = "categoria", key = "#id")
    @Override
    public CategoriaDTOconImagen LeerCategoria(int id) {
        System.out.println("⚠️ Leer categoria");
        Categoria categoria = new Categoria();
        categoria.setId(id);

        CategoriaEntity entidad = categoriaRepository.LeerUnoSinImagen(categoria);
        if (entidad == null) {
            throw new IllegalArgumentException("Categoría no encontrada");
        }

        boolean esDestacada = jpaCategoriaDestacadoRepository.findAll().stream()
                .anyMatch(dest -> dest.getCategoria().getId().equals(entidad.getId()));

        // Buscar la imagen
        Optional<CategoriaArchivoEntity> archivo = jpaCategoriaArchivoRepository.findByCategoriaId_Id(entidad.getId());
        String linkArchivo = archivo.map(CategoriaArchivoEntity::getLinkArchivo).orElse(null);


        return CategoriaDTOconImagen.builder()
                .id(entidad.getId())
                .nombre(entidad.getNombre())
                .descripcion(entidad.getDescripcion())
                .destacada(esDestacada)
                .linkArchivo(linkArchivo)
                .build();
    }

    @Override
    public Categoria ModificarCategoria(Categoria categoria, int id) {
        System.out.println("⚠️ Modificando categoria");
        if (categoriaRepository.buscarPorNombreIgnoreCase(categoria.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        Categoria modificada = categoriaRepository.modificar(categoria, id);
        cachearCategoriaSinImagen(id);
        return modificada;
    }


    // Modificar imagen = borra solo la categoría individual
    @Override
    public boolean ModificarCategoriaImagen(MultipartFile archivoNuevo, int id) throws IOException {
        // Buscar categoría para obtener la entidad
        CategoriaEntity categoriaEntity = jpaCategoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Buscar imagen existente
        Optional<CategoriaArchivoEntity> archivoExistenteOpt = jpaCategoriaArchivoRepository.findByCategoriaId_Id(id);

        // Borrar imagen previa si existe
        if (archivoExistenteOpt.isPresent()) {
            CategoriaArchivoEntity archivoExistente = archivoExistenteOpt.get();
            imgBBUploaderService.borrarImagenDeImgBB(archivoExistente.getDeleteUrl());
            jpaCategoriaArchivoRepository.delete(archivoExistente);
        }

        // Subir nueva imagen
        ImgBBData data = imgBBUploaderService.subirImagen(archivoNuevo);

        // Guardar nueva imagen en BDD
        CategoriaArchivoEntity nuevoArchivo = new CategoriaArchivoEntity();
        nuevoArchivo.setCategoriaId(categoriaEntity);
        nuevoArchivo.setLinkArchivo(data.getUrl());
        nuevoArchivo.setDeleteUrl(data.getDelete_url());

        jpaCategoriaArchivoRepository.save(nuevoArchivo);

        return true;
    }


    @Override
    public void BorrarCategoria(int id) {
        System.out.println("⚠️ Borrando categoria");
        String url = categoriaRepository.borrarImagen(id);
        if (url != null) {
            imgBBUploaderService.borrarImagenDeImgBB(url);
        }

        categoriaRepository.borrar(id);

        // 🔥 Borrar del caché individual
        Optional.ofNullable(cacheManager.getCache("categoria"))
                .ifPresent(cache -> cache.evict(id));

        // 🧹 Borrar de la lista cacheada
        Optional.ofNullable(cacheManager.getCache("categorias"))
                .ifPresent(cache -> {
                    Object listaObj = cache.get("lista", Object.class);
                    if (listaObj instanceof List<?> original) {
                        List<CategoriaDTO> lista = new ArrayList<>((List<CategoriaDTO>) original);
                        lista.removeIf(c -> c.getId() == id);
                        cache.put("lista", lista);
                    }
                });

        // ✅ Actualizar cache del combo de categorías
        Optional.ofNullable(cacheManager.getCache("categoriasCombo"))
                .ifPresent(cache -> {
                    Object comboObj = cache.get("combo", Object.class);
                    if (comboObj instanceof List<?> original) {
                        List<CategoriaComboDTO> combo = new ArrayList<>((List<CategoriaComboDTO>) original);
                        combo.removeIf(dto -> dto.getId() == id);
                        cache.put("combo", combo);
                    }
                });


    }


    @Override
    public void toggleCategoriaDestacada(int id) {
        System.out.println("⚠️ Aplicando destacado");
        Categoria categoria = new Categoria();
        categoria.setId(id);

        CategoriaEntity categoriaEntity = categoriaRepository.LeerUnoSinImagen(categoria);
        if (categoriaEntity == null) {
            throw new RuntimeException("Categoría no encontrada");
        }

        Optional<CategoriaDestacadoEntity> existente = jpaCategoriaDestacadoRepository
                .findByCategoria(categoriaEntity);

        boolean esDestacadaAhora;
        if (existente.isPresent()) {
            jpaCategoriaDestacadoRepository.delete(existente.get());
            esDestacadaAhora = false;
        } else {
            long cantidad = jpaCategoriaDestacadoRepository.count();
            if (cantidad >= 3) {
                throw new MaxDestacadosException("No se pueden destacar más de 3 categorías");
            }

            CategoriaDestacadoEntity nuevo = CategoriaDestacadoEntity.builder()
                    .categoria(categoriaEntity)
                    .build();
            jpaCategoriaDestacadoRepository.save(nuevo);
            esDestacadaAhora = true;
        }

        // 🧹 Limpiar cache individual
        Optional.ofNullable(cacheManager.getCache("categoria"))
                .ifPresent(cache -> cache.evict(id));

        // ♻️ Actualizar la lista cacheada
        Optional.ofNullable(cacheManager.getCache("categorias"))
                .ifPresent(cache -> {
                    Object listaObj = cache.get("lista", Object.class);
                    if (listaObj instanceof List<?> original) {
                        List<CategoriaDTO> lista = new ArrayList<>((List<CategoriaDTO>) original);
                        lista.replaceAll(dto -> dto.getId() == id
                                ? new CategoriaDTO(dto.getId(), dto.getNombre(), dto.getDescripcion(), esDestacadaAhora)
                                : dto);
                        cache.put("lista", lista);
                    }
                });

        // ✅ Actualizar cache del combo de categorías
        Optional.ofNullable(cacheManager.getCache("categoriasCombo"))
                .ifPresent(cache -> {
                    Object comboObj = cache.get("combo", Object.class);
                    if (comboObj instanceof List<?> original) {
                        List<CategoriaComboDTO> combo = new ArrayList<>((List<CategoriaComboDTO>) original);
                        combo.removeIf(dto -> dto.getId() == id); // 💥 eliminamos solo la categoría borrada
                        cache.put("combo", combo); // ✅ actualizamos sin invalidar todo
                    }
                });
    }



    // Leer combo de categorías (opcionalmente cacheado)
    @Cacheable(value = "categoriasCombo", key = "'combo'")
    @Override
    public List<CategoriaComboDTO> LeerCategoriasCombo() {
        List<CategoriaDestacadoEntity> destacados = jpaCategoriaDestacadoRepository.findAll();

        return jpaCategoriaRepository.findAll().stream()
                .map(c -> {
                    boolean esDestacada = destacados.stream()
                            .anyMatch(d -> d.getCategoria().getId().equals(c.getId()));

                    // Buscar imagen de la categoría
                    String imagen = jpaCategoriaArchivoRepository.findByCategoriaId_Id(c.getId())
                            .map(CategoriaArchivoEntity::getLinkArchivo)
                            .orElse(null);

                    return new CategoriaComboDTO(c.getId(), c.getNombre(), esDestacada, imagen);
                })
                .toList();
    }
}
