package com.example.WarmTea.Controller;


import com.example.WarmTea.Service.MLService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
public class MLController {

    private final MLService mlService;

    @Operation(summary = "Переобучить ML-модель", description = "Запускает переобучение модели на актуальных данных из БД")
    @ApiResponse(responseCode = "200", description = "Переобучение запущено")
    @PostMapping("/retrain")
    public ResponseEntity<Map<String, Object>> retrain() {
        Map<String, Object> result = mlService.retrainModel();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Проверить состояние ML-сервиса", description = "Возвращает статус ML-сервиса и метрики")
    @ApiResponse(responseCode = "200", description = "Сервис работает")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = mlService.healthCheck();
        return ResponseEntity.ok(result);
    }
}
