package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.TrackDto;
import com.example.WarmTea.Models.Episode;
import com.example.WarmTea.Repository.EpisodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VideoController {

    @Autowired
    private EpisodeRepository episodeRepository;

    @GetMapping("/episodes/{id}")
    public ResponseEntity<?> getEpisode(@PathVariable Long id) {
        // Находим эпизод или выбрасываем 404
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Эпизод не найден"));

        // Превращаем список MediaTrack в DTO для фронтенда
        List<TrackDto> tracks = episode.getMediaTracks().stream()
                .map(t -> new TrackDto(
                        t.getSrcUrl(),      // Метод теперь существует
                        t.getLabel(),       // Метод теперь существует
                        t.getLanguageCode(),
                        t.getType(),
                        t.isDefault()
                )).toList();

        return ResponseEntity.ok(Map.of(
                "title", episode.getTitle(),
                "videoUrl", episode.getVideoUrl(),
                "tracks", tracks
        ));
    }
}