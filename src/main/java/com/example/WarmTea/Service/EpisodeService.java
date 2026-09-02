package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.EpisodeDto;
import com.example.WarmTea.Models.Content;
import com.example.WarmTea.Models.Episode;
import com.example.WarmTea.Repository.ContentRepository;
import com.example.WarmTea.Repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final ContentRepository contentRepository;

    @Transactional(readOnly = true)
    public List<EpisodeDto> getEpisodesByContent(Long contentId) {
        return episodeRepository.findByContentIdOrderBySeasonNumberAscEpisodeNumberAsc(contentId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EpisodeDto updateEpisode(Long episodeId, EpisodeDto dto) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Эпизод не найден"));
        episode.setSeasonNumber(dto.getSeasonNumber());
        episode.setEpisodeNumber(dto.getEpisodeNumber());
        episode.setTitle(dto.getTitle());
        episode.setVideoUrl(dto.getVideoUrl());
        episode.setDuration(dto.getDuration());
        episode.setThumbnail(dto.getThumbnail());
        return toDto(episodeRepository.save(episode));
    }

    @Transactional
    public EpisodeDto createEpisode(EpisodeDto dto) {
        Content content = contentRepository.findById(dto.getContentId())
                .orElseThrow(() -> new RuntimeException("Контент не найден"));
        if (content.getType().name().equals("MOVIE")) {
            throw new RuntimeException("Невозможно добавить эпизод к фильму");
        }
        Episode episode = new Episode();
        episode.setContent(content);
        episode.setSeasonNumber(dto.getSeasonNumber());
        episode.setEpisodeNumber(dto.getEpisodeNumber());
        episode.setTitle(dto.getTitle());
        episode.setVideoUrl(dto.getVideoUrl());
        episode.setDuration(dto.getDuration());
        episode.setThumbnail(dto.getThumbnail());
        Episode saved = episodeRepository.save(episode);
        return toDto(saved);  // <-- теперь возвращаем DTO
    }

    @Transactional
    public void deleteEpisode(Long id) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Эпизод не найден"));
        episodeRepository.delete(episode);
    }

    private EpisodeDto toDto(Episode episode) {
        return EpisodeDto.builder()
                .id(episode.getId())
                .seasonNumber(episode.getSeasonNumber())
                .episodeNumber(episode.getEpisodeNumber())
                .title(episode.getTitle())
                .videoUrl(episode.getVideoUrl())
                .duration(episode.getDuration())
                .thumbnail(episode.getThumbnail())
                .build();
    }
}