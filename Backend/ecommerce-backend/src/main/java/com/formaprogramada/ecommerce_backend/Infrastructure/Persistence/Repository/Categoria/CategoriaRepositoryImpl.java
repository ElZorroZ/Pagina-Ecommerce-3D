package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Categoria.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderServiceImpl;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaDestacadoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Categoria.CategoriaEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public class CategoriaRepositoryImpl implements CategoriaRepository {
    private final JpaCategoriaRepository jpaRepository;
    private final CategoriaEntityMapper mapper;
    private final JpaCategoriaBuscarRepository jpaRepository2;
    private final JpaCategoriaArchivoRepository jpaCategoriaArchivoRepository;
    private final JpaCategoriaDestacadoRepository jpaCategoriaDestacadoRepository;
    @Autowired
    private final ImgBBUploaderServiceImpl imgBBUploaderService;
    @Autowired
    private final JpaCategoriaRepository jpaCategoriaRepository;

    public CategoriaRepositoryImpl(JpaCategoriaRepository jpaRepository,
                                   CategoriaEntityMapper mapper,
                                   JpaCategoriaBuscarRepository jpaRepository2,
                                   JpaCategoriaArchivoRepository jpaCategoriaArchivoRepository,
                                   JpaCategoriaDestacadoRepository jpaCategoriaDestacadoRepository,
                                   JpaCategoriaRepository jpaCategoriaRepository,
                                   ImgBBUploaderServiceImpl imgBBUploaderService) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.jpaRepository2 = jpaRepository2;
        this.jpaCategoriaArchivoRepository = jpaCategoriaArchivoRepository;
        this.jpaCategoriaDestacadoRepository = jpaCategoriaDestacadoRepository;
        this.imgBBUploaderService=imgBBUploaderService;
        this.jpaCategoriaRepository=jpaCategoriaRepository;
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        CategoriaEntity entity = mapper.toEntity(categoria);
        CategoriaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Map<CategoriaEntity,String> LeerTodo(Map<CategoriaEntity,String> lista) {
        List<CategoriaEntity> totalCategoria=jpaRepository2.findAll();
        List<CategoriaArchivoEntity>totalImagen=jpaCategoriaArchivoRepository.findAll();
        for (CategoriaEntity categoriaEntity : totalCategoria) {
            for(CategoriaArchivoEntity categoriaArchivo: totalImagen){
                if (categoriaArchivo.getId().equals(categoriaEntity.getId())){
                    lista.put(categoriaEntity,categoriaArchivo.getLinkArchivo());
                }
            }
        }

        return lista;
    }

    @Override
    public Map<CategoriaEntity,String> LeerUno(Categoria categoria) {
        CategoriaEntity categoriaEntity = jpaRepository.findById(categoria.getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        CategoriaArchivoEntity imagen = jpaCategoriaArchivoRepository.findById(categoria.getId()).orElseThrow(null);
        Map<CategoriaEntity, String> lista = new HashMap<CategoriaEntity, String>();
        String link=imagen.getLinkArchivo();
        lista.put(categoriaEntity,link);
        return lista;

    }

    public CategoriaEntity LeerUnoSinImagen(Categoria categoria) {
        return jpaRepository.findById(categoria.getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }




    @Override
    public Categoria modificar(Categoria categoria, int id) {
        CategoriaEntity entity = mapper.toEntity(categoria);

        CategoriaEntity updateEntity=jpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        updateEntity.setNombre(categoria.getNombre());
        updateEntity.setDescripcion(categoria.getDescripcion());

        jpaRepository.save(updateEntity);
        return categoria;
    }

    @Override
    public void borrar(int id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public String borrarImagen(int categoriaId) {
        // 1. Buscar archivo asociado
        Optional<CategoriaArchivoEntity> archivoOpt = jpaCategoriaArchivoRepository.findByCategoriaId_Id(categoriaId);

        if (archivoOpt.isPresent()) {
            // 2. Borrar imagen externa
            imgBBUploaderService.borrarImagenDeImgBB(archivoOpt.get().getDeleteUrl());
            // 3. Borrar registro del archivo
            jpaCategoriaArchivoRepository.delete(archivoOpt.get());
        }

        // 4. Borrar categoría (y cascada en la BDD elimina cualquier relación restante)
        jpaCategoriaRepository.deleteById(categoriaId);
        return null;
    }


    @Override
    public boolean AgregarDestacado(CategoriaEntity cat2) {


        CategoriaDestacadoEntity caId = new CategoriaDestacadoEntity();
        caId.setCategoria(cat2);
        jpaCategoriaDestacadoRepository.save(caId);

        return true;
    }
    @Override
    public Optional<Categoria> buscarPorNombreIgnoreCase(String nombre) {
        return jpaCategoriaRepository.findByNombreIgnoreCase(nombre)
                .map(mapper::toDomain);
    }


}
