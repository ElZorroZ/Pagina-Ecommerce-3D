package com.formaprogramada.ecommerce_backend;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@Profile("prod") // solo se activa en profile 'prod'
@EnableElasticsearchRepositories(
        basePackages = "com.formaprogramada.ecommerce_backend.Infrastructure.Elastic"
)
public class ElasticsearchConfig {

    private final ElasticsearchClient client;

    public ElasticsearchConfig(ElasticsearchClient client) {
        this.client = client;
    }

    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchTemplate(client); // aquí se usa ElasticsearchTemplate
    }
}
