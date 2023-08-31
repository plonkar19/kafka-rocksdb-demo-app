package com.example.kafkarocksdbdemoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaRocksdbDemoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaRocksdbDemoAppApplication.class, args);
    }

}
