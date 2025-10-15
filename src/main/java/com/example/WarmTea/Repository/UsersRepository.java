package com.example.WarmTea.Repository;

import com.example.WarmTea.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    // Поиск пользователя по email
    Users findByEmail(String email);

    // Поиск пользователя по username
    Optional<Users> findByUsername(String username);
}
