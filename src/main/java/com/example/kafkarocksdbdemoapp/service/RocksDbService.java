package com.example.kafkarocksdbdemoapp.service;

import java.util.Map;

import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.kafkarocksdbdemoapp.dao.RocksDbDao;

@Service
public class RocksDbService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDbService.class);

    private final RocksDbDao rocksDbDao;

    public RocksDbService() {
        this.rocksDbDao = new RocksDbDao("/tmp/rocksdb", null);
    }

    public void save(String key, String value) {
        try {
            rocksDbDao.putData(key.getBytes(), value.getBytes());
        } catch (RocksDBException e) {
            LOGGER.error("Error saving entry in RocksDB, cause: {}, message: {}", e.getCause(), e.getMessage());
        }
    }

    public String find(String key) {
        try {
            return rocksDbDao.getData(key.getBytes());
        } catch (RocksDBException e) {
            LOGGER.error("Error retrieving the entry in RocksDB from key: {}, cause: {}, message: {}", key, e.getCause(), e.getMessage());
        }

        return null;
    }

    public void delete(String key) {
        try {
            rocksDbDao.deleteData(key.getBytes());
        } catch (RocksDBException e) {
            LOGGER.error("Error deleting entry in RocksDB, cause: {}, message: {}", e.getCause(), e.getMessage());
        }
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = rocksDbDao.getStatistics();

        LOGGER.info("RocksDB Statistics: {}", stats.toString());

        return stats;
    }
}
