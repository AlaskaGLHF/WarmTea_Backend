package com.example.WarmTea.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerService {

    private final MLService mlService;

    // Каждую неделю в 3:00 ночи по воскресеньям
    @Scheduled(cron = "0 0 3 * * SUN")
    public void retrainModelWeekly() {
        log.info("Запуск еженедельного переобучения ML-модели...");
        try {
            Map<String, Object> result = mlService.retrainModel();
            log.info("Переобучение завершено. Результат: {}", result);
        } catch (Exception e) {
            log.error("Ошибка при переобучении модели: ", e);
        }
    }
}