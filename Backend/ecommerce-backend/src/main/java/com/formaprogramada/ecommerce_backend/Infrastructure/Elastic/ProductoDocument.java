package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ColorRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(indexName = "productos")
public class ProductoDocument {

    @Id
    private Integer id;

    @Field(type = FieldType.Text, analyzer = "spanish")
    private String nombre;

    @Field(type = FieldType.Float)
    private Float precio;

    @Field(type = FieldType.Text)
    private List<String> tags; // Búsqueda adicional
}
