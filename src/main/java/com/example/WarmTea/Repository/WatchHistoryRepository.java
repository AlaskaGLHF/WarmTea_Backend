package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    Optional<WatchHistory> findByUserIdAndContentIdAndEpisodeId(Long userId, Long contentId, Long episodeId);
    // WatchHistoryRepository.java
    List<WatchHistory> findByUserIdOrderByWatchedAtDesc(Long userId);

    List<WatchHistory> findByUserIdAndContentIdOrderByWatchedAtDesc(Long userId, Long contentId);
}

