package com.example.WarmTea.Repository;

import com.example.WarmTea.Dtos.AverageRatingResponseDto;
import com.example.WarmTea.Models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndContentId(Long userId, Long contentId);

    List<Rating> findByUserId(Long userId);

    List<Rating> findByContentId(Long contentId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.content.id = :contentId")
    Double getAverageScoreForContent(@Param("contentId") Long contentId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.content.id = :contentId")
    Long getVotesCountForContent(@Param("contentId") Long contentId);

    // RatingRepository.java
    @Query("SELECT new com.example.WarmTea.Dtos.AverageRatingResponseDto(r.content.id, AVG(r.score), COUNT(r)) " +
            "FROM Rating r WHERE r.content.id IN :contentIds GROUP BY r.content.id")
    List<AverageRatingResponseDto> getAverageScoresForContentIds(@Param("contentIds") List<Long> contentIds);

    @Query("SELECT r.content.id, COUNT(r) FROM Rating r WHERE r.content.id IN :contentIds GROUP BY r.content.id")
    List<Object[]> getVotesCountForContentIds(@Param("contentIds") List<Long> contentIds);
}