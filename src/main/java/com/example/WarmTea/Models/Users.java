package com.example.WarmTea.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Data // ✅ Lombok: автоматически генерирует геттеры, сеттеры, toString, equals и hashCode
@NoArgsConstructor // ✅ Lombok: создаёт конструктор без аргументов (требуется JPA)
@AllArgsConstructor // ✅ Lombok: создаёт конструктор со всеми полями
@Builder // ✅ Lombok: создаёт паттерн Builder для удобного создания объектов
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password_hash;

    private OffsetDateTime created_at = OffsetDateTime.now();
}
