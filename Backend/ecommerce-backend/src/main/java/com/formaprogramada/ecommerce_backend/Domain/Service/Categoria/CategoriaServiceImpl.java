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

    // Crear categoría (con o sin imagen) → invalida la lista
    @Caching(evict = {
        @CacheEvict(value = "categorias", allEntries = true),
        @CacheEvict(value = "categoriasCombo", allEntries = true)
        }
    )
    @Override
    public Categoria CrearCategoria(Categoria categoria) {
        // Verificar si ya existe una categoría con ese nombre (ignorar mayúsculas/minúsculas)
        if (categoriaRepository.buscarPorNombreIgnoreCase(categoria.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        return categoriaRepository.guardar(categoria);
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
    @Cacheable(value = "categorias")
    @Override
    public List<CategoriaDTO> LeerCategorias() {
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

    // Modificar categoría → actualiza individual y borra lista
    @Caching(
        put = @CachePut(value = "categoria", key = "#id"),
        evict = {
            @CacheEvict(value = "categorias", allEntries = true),
            @CacheEvict(value = "categoriasCombo", allEntries = true)
        }
    )
    @Override
    public Categoria ModificarCategoria(Categoria categoria, int id) {
        if (categoriaRepository.buscarPorNombreIgnoreCase(categoria.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }
        return categoriaRepository.modificar(categoria,id);
    }

    // Modificar imagen = borra solo la categoría individual
    @CacheEvict(value = "categoria", key = "#id")
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

    // Borrar categoría → borra individual y lista
    @Caching(evict = {
        @CacheEvict(value = "categoria", key = "#id"),
        @CacheEvict(value = "categorias", allEntries = true),
        @CacheEvict(value = "categoriasCombo", allEntries = true)
        }
    )
    @Override
    public void BorrarCategoria(int id) {
        String url = categoriaRepository.borrarImagen(id);
        if (url != null) {
            imgBBUploaderService.borrarImagenDeImgBB(url);
        }
        categoriaRepository.borrar(id);
    }

    // Destacar o quitar destacado → puede afectar lista y detalle
    @Caching(evict = {
        @CacheEvict(value = "categoria", key = "#id"),
        @CacheEvict(value = "categorias", allEntries = true)
        }
    )
    @Override
    public void toggleCategoriaDestacada(int id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);

        CategoriaEntity categoriaEntity = categoriaRepository.LeerUnoSinImagen(categoria);
        if (categoriaEntity == null) {
            throw new RuntimeException("Categoría no encontrada");
        }

        Optional<CategoriaDestacadoEntity> existente = jpaCategoriaDestacadoRepository
                .findByCategoria(categoriaEntity);

        if (existente.isPresent()) {
            jpaCategoriaDestacadoRepository.delete(existente.get());
        } else {
            long cantidad = jpaCategoriaDestacadoRepository.count();
            if (cantidad >= 3) {
                throw new MaxDestacadosException("No se pueden destacar más de 3 categorías");
            }

            CategoriaDestacadoEntity nuevo = CategoriaDestacadoEntity.builder()
                    .categoria(categoriaEntity)
                    .build();
            jpaCategoriaDestacadoRepository.save(nuevo);
        }
    }

    // Leer combo de categorías (opcionalmente cacheado)
    @Cacheable(value = "categoriasCombo")
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
