package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.ContentDto;
import com.example.WarmTea.Dtos.MLDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MLService {
    private final WebClient webClient;
    private final ContentService contentService;

    public List<ContentDto.ShortContentDto> getRecommendations(Long userId, int limit) {
        try {
            MLDto.RecommendationRequestDto request = MLDto.RecommendationRequestDto.builder()
                    .userId(userId)
                    .n(limit)
                    .build();

            MLDto.RecommendationResponseDto response = webClient.post()
                    .uri("/recommendations")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("ML service error: " + error)))
                    )
                    .bodyToMono(MLDto.RecommendationResponseDto.class)
                    .block();

            log.info("ML service response: {}", response);
            if (response == null || response.getRecommendations() == null || response.getRecommendations().isEmpty()) {
                log.info("ML returned empty, using fallback (random content)");
                return contentService.getRandomContentByType("MOVIE", limit);
            }

            List<Long> contentIds = response.getRecommendations().stream()
                    .map(MLDto.RecommendationItemDto::getId)
                    .collect(Collectors.toList());

            log.info("Extracted content IDs: {}", contentIds);

            List<ContentDto.ShortContentDto> shortContent = contentService.getContentShortByIds(contentIds);
            Map<Long, ContentDto.ShortContentDto> map = shortContent.stream()
                    .collect(Collectors.toMap(ContentDto.ShortContentDto::getId, Function.identity()));

            List<ContentDto.ShortContentDto> result = contentIds.stream()
                    .map(map::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (result.isEmpty()) {
                log.info("Result after mapping is empty, using fallback (random content)");
                return contentService.getRandomContentByType("MOVIE", limit);
            }

            log.info("Returning {} recommendations", result.size());
            return result;

        } catch (Exception e) {
            log.error("ML service error, using fallback (random content)", e);
            return contentService.getRandomContentByType("MOVIE", limit);
        }
    }

    public Map<String, Object> retrainModel() {
        return webClient.post()
                .uri("/retrain")
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("ML retrain error: " + error)))
                )
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> healthCheck() {
        return webClient.get()
                .uri("/health")
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("ML health check error: " + error)))
                )
                .bodyToMono(Map.class)
                .block();
    }
}