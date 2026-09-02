package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findByContentIdOrderBySeasonNumberAscEpisodeNumberAsc(Long contentId);
    void deleteByContentId(Long contentId);
}