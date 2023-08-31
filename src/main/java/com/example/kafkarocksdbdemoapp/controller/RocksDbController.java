package com.example.kafkarocksdbdemoapp.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.kafkarocksdbdemoapp.service.RocksDbService;

@RestController
public class RocksDbController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDbController.class);
    private final RocksDbService rocksDbService;

    public RocksDbController(RocksDbService rocksDbService) {
        this.rocksDbService = rocksDbService;
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> find(@PathVariable("key") String key) {
        String result = rocksDbService.find(key);
        if (result == null)
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{key}")
    public ResponseEntity<String> save(@PathVariable("key") String key, @RequestBody String value) {
        rocksDbService.save(key, value);

        return ResponseEntity.ok(value);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<String> delete(@PathVariable("key") String key) {
        rocksDbService.delete(key);

        return ResponseEntity.ok(key);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> find() {
        Map<String, Object> result = rocksDbService.getStatistics();

        if (result == null)
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(result);
    }
}
