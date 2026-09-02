package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.GenresLink;
import com.example.WarmTea.Models.ContentGenreKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenresLinkRepository extends JpaRepository<GenresLink, ContentGenreKey> {
    void deleteByContentId(Long contentId);

    // GenresLinkRepository.java
    void deleteByContentIdAndGenreId(Long contentId, Long genreId);
}