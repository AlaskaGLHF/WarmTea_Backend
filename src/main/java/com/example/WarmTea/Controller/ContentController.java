package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.ContentDto;
import com.example.WarmTea.Service.ContentService;
import com.example.WarmTea.Service.MLService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final MLService mlService;

    @Operation(summary = "Получить весь контент", description = "Возвращает полный список фильмов, сериалов и аниме")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContentDto.ContentResponseDto.class)
                    )
            )
    })
    @GetMapping("/full")
    public List<ContentDto.ContentResponseDto> getAllContent() {
        return contentService.getAllContent();
    }

    @Operation(summary = "Получить каталог контента (кратко)", description = "Возвращает страницу контента в кратком формате для плиток")
    @GetMapping
    public ResponseEntity<Page<ContentDto.ShortContentDto>> getAllContentShort(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {

        return ResponseEntity.ok(contentService.getAllContentShort(page, size));

    }

    @Operation(summary = "Получить контент по ID", description = "Возвращает полную информацию о контенте (включая серии для сериалов)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Контент найден"),
            @ApiResponse(responseCode = "404", description = "Контент не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContentDto.ContentResponseDto> getContentById(@PathVariable Long id) {

        ContentDto.ContentResponseDto content = contentService.getContentById(id);
        return content != null ? ResponseEntity.ok(content) : ResponseEntity.notFound().build();

    }

    @Operation(summary = "Создать новый контент", description = "Добавляет фильм/сериал/аниме и загружает файлы на S3")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentDto.ContentResponseDto> createContent(
            @Valid @ModelAttribute ContentDto.ContentRequestDto dto
    ) throws IOException {

        ContentDto.ContentResponseDto created = contentService.createContent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    @Operation(summary = "Обновить данные контента")
    @PutMapping("/{id}")
    public ResponseEntity<ContentDto.ContentResponseDto> updateContent(

            @PathVariable Long id,
            @Valid @RequestBody ContentDto.ContentRequestDto dto

    ) throws IOException {

        ContentDto.ContentResponseDto updated = contentService.updateContent(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();

    }

    @Operation(summary = "Удалить контент")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {

        return contentService.deleteContent(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();

    }

    @Operation(summary = "Поиск контента по жанрам", description = "Например: /api/content/search?genres=action,sci-fi")
    @GetMapping("/search")
    public List<ContentDto.ContentResponseDto> searchContentByGenres(@RequestParam String genres) {
        List<String> genreList = Arrays.stream(genres.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        return contentService.getContentByGenres(genreList);
    }

    @Operation(summary = "Добавить в избранное", description = "Принимает userId и contentId в теле запроса")
    @PostMapping("/favorites/add")
    public ResponseEntity<Void> addToFavorites(@RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        Long contentId = payload.get("contentId");

        if (userId == null || contentId == null) {

            return ResponseEntity.badRequest().build();

        }

        contentService.addToFavorites(userId, contentId);
        return ResponseEntity.ok().build();

    }

    @Operation(summary = "Удалить из избранного", description = "Удаляет связь контента с пользователем")
    @DeleteMapping("/favorites/remove/{userId}/{contentId}")
    public ResponseEntity<Void> removeFromFavorites(

            @PathVariable Long userId,
            @PathVariable Long contentId

    ) {

        contentService.removeFromFavorites(userId, contentId);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/favorites/ids/{userId}")
    public ResponseEntity<List<Long>> getFavoriteIds(@PathVariable Long userId) {
        return ResponseEntity.ok(contentService.getFavoriteIds(userId));
    }

    @Operation(summary = "Получить полные данные избранного для профиля")
    @GetMapping("/favorites/full/{userId}")
    public ResponseEntity<List<ContentDto.ShortContentDto>> getFullFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(contentService.getFullFavorites(userId));
    }

    @DeleteMapping("/{contentId}/genres/{genreId}")
    public ResponseEntity<Void> removeGenreFromContent(
            @PathVariable Long contentId,
            @PathVariable Long genreId) {
        contentService.removeGenreFromContent(contentId, genreId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ContentDto.ShortContentDto>> getRecommendations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<ContentDto.ShortContentDto> recommendations = mlService.getRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

}