package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    // Spring Boot сам создаст реализацию на основе имени метода
    boolean existsByUserAndContent(User user, Content content);

    void deleteByUserAndContent(User user, Content content);

    List<Favorite> findByUser(User user);
}