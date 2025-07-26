package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductosPorCategoriaCacheKeys {
    private final Set<String> keys = ConcurrentHashMap.newKeySet();

    public void addKey(String key) {
        keys.add(key);
    }

    public void removeKey(String key) {
        keys.remove(key);
    }

    public Set<String> getKeys() {
        return Set.copyOf(keys);
    }
}
