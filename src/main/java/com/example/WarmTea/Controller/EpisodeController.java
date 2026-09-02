package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.EpisodeDto;
import com.example.WarmTea.Models.Episode;
import com.example.WarmTea.Service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/episodes")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;

    @GetMapping("/content/{contentId}")
    public ResponseEntity<List<EpisodeDto>> getEpisodesByContent(@PathVariable Long contentId) {
        return ResponseEntity.ok(episodeService.getEpisodesByContent(contentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EpisodeDto> updateEpisode(@PathVariable Long id, @RequestBody EpisodeDto dto) {
        return ResponseEntity.ok(episodeService.updateEpisode(id, dto));
    }

    @PostMapping
    public ResponseEntity<EpisodeDto> createEpisode(@RequestBody EpisodeDto dto) {
        EpisodeDto created = episodeService.createEpisode(dto); // <-- получаем DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEpisode(@PathVariable Long id) {
        episodeService.deleteEpisode(id);
        return ResponseEntity.noContent().build();
    }
}