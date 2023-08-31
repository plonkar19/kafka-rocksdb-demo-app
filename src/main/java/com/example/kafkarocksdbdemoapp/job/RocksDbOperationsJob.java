package com.example.kafkarocksdbdemoapp.job;

import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.kafkarocksdbdemoapp.service.RocksDbService;

@Component
public class RocksDbOperationsJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDbOperationsJob.class);
    private final RocksDbService rocksDbService;

    public RocksDbOperationsJob(RocksDbService rocksDbService) {
        this.rocksDbService = rocksDbService;
    }

    @Scheduled(fixedRate = 1000)
    public void rocksDbWrite() {
        int n = new Random().nextInt(10 - 1 + 1) + 1;
        UUID uuid = UUID.randomUUID();
        String value = uuid.toString();

        for (int i = 0; i < n; i++) {
            int num = new Random().nextInt(100 - 1 + 1) + 1;
            String key = "key" + num;
            rocksDbService.save(key, value);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void rocksDbRead() {
        int n = new Random().nextInt(10 - 1 + 1) + 1;

        for (int i = 0; i < n; i++) {
            int num = new Random().nextInt(100 - 1 + 1) + 1;
            String key = "key" + num;
            rocksDbService.find(key);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void getStatistics() {
//        rocksDbService.getStatistics();
    }
}
