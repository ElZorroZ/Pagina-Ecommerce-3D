package com.formaprogramada.ecommerce_backend;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@Configuration
@Profile("prod")
public class ElasticsearchConfig {

    /*@Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean(name = "elasticsearchTemplate")
    public ElasticsearchOperations elasticsearchOperations(ElasticsearchClient client) {
        return new ElasticsearchTemplate(client);
    }

     */
}

