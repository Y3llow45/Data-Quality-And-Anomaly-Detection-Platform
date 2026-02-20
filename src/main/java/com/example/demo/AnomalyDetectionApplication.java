package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AnomalyDetectionApplication {
	public static void main(String[] args) {
		SpringApplication.run(AnomalyDetectionApplication.class, args);
	}
}
