package com.example.WarmTea;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
@Slf4j
public class WarmTeaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarmTeaApplication.class, args);
        openSwagger();
    }

    @Bean
    public static CommandLineRunner openSwagger() {
        return args -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI("http://localhost:8080/swagger-ui.html"));
                    log.info("Swagger UI открыт: http://localhost:8080/swagger-ui.html");
                }
            } catch (Exception e) {
                log.error("Не удалось открыть Swagger автоматически: {}", e.getMessage());
            }
        };
    }
}
