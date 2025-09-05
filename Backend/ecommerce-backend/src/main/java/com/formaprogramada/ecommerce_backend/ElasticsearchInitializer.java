package com.formaprogramada.ecommerce_backend;

import com.formaprogramada.ecommerce_backend.Infrastructure.Elastic.ProductoSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
@Profile("prod") // Solo se carga en prod
@Component
public class ElasticsearchInitializer implements CommandLineRunner {

    @Autowired(required = false)
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ProductoSyncService productoSyncService;

    @Override
    public void run(String... args) throws Exception {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("productos");
        IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);

        // Crear índice si no existe
        if (!indexOperations.exists()) {
            System.out.println("Creando índice 'productos' en Elasticsearch...");
            indexOperations.create();

            // Configurar mapeo personalizado si es necesario
            String mapping = """
            {
              "properties": {
                "nombre": {
                  "type": "text",
                  "analyzer": "spanish",
                  "fields": {
                    "keyword": {
                      "type": "keyword"
                    }
                  }
                },
                "precio": {
                  "type": "float"
                },
                "precioDigital": {
                  "type": "float"
                }
              }
            }
            """;

            indexOperations.putMapping(Document.parse(mapping));
        }

        // Sincronizar siempre, exista o no el índice
        System.out.println("Iniciando sincronización de productos...");
        productoSyncService.sincronizarTodosLosProductos();
    }
}
