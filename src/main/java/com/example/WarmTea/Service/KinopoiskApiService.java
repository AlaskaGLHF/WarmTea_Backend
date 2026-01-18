package com.example.WarmTea.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Slf4j
public class KinopoiskApiService {

    private static final String API_URL = "https://api.kinopoisk.dev/v1.4/movie";

    @Value("${kinopoisk.api-token}")
    private String apiToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public KinopoiskApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public MovieApiResponse getMovie(Long kpId) {
        if (kpId == null) return null;

        String url = API_URL + "/" + kpId;
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readValue(response.getBody(), MovieApiResponse.class);
            } else {
                log.warn("Kinopoisk API вернул статус {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Ошибка при запросе к Kinopoisk API для kpId={}", kpId, e);
        }

        return null;
    }

    public Optional<Ratings> getRatings(Long kpId) {
        if (kpId == null || kpId <= 0) return Optional.empty();
        MovieApiResponse movie = getMovie(kpId);
        return movie != null && movie.getRating() != null
                ? Optional.of(movie.getRating())
                : Optional.empty();
    }

    public MovieApiResponse[] searchMovies(String queryParams) {
        try {
            String encodedQuery = URLEncoder.encode(queryParams, StandardCharsets.UTF_8);
            URI uri = UriComponentsBuilder.fromHttpUrl(API_URL)
                    .query(encodedQuery)
                    .build(true)
                    .toUri();

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readValue(response.getBody(), MovieApiResponse[].class);
            } else {
                log.warn("Kinopoisk API вернул статус {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Ошибка при поиске фильмов по query: {}", queryParams, e);
        }

        return new MovieApiResponse[0];
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MovieApiResponse {
        private Long id;
        private String name;
        private Ratings rating;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ratings {
        private Double kp;
        private Double imdb;
    }
}
