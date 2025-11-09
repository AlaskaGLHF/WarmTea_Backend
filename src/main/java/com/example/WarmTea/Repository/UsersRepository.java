package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {

    // Поиск пользователя по email
    User findByEmail(String email);

    // Поиск пользователя по username
    Optional<User> findByUsername(String username);
}
