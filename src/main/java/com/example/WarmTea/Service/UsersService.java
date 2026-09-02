package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.ChangePasswordRequestDTO;
import com.example.WarmTea.Dtos.UsersDto.UserRequestDTO;
import com.example.WarmTea.Dtos.UsersDto.UserResponseDTO;
import com.example.WarmTea.Models.User;
import com.example.WarmTea.Repository.UsersRepository;
import com.example.WarmTea.Utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // === Получить всех пользователей ===
    public List<UserResponseDTO> getAllUsers() {
        return usersRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // === Получить пользователя по ID ===
    public UserResponseDTO getUserById(Long id) {
        return usersRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    // === Получить пользователя по email ===
    public UserResponseDTO getUserByEmail(String email) {
        User user = usersRepository.findByEmail(email);
        return user != null ? toDTO(user) : null;
    }

    // === Обновление пользователя ===
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User existing = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка уникальности username (если изменяется)
        if (!existing.getUsername().equals(request.getUsername())) {
            if (usersRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Имя пользователя уже занято");
            }
            existing.setUsername(request.getUsername());
        }

        // Проверка уникальности email (если изменяется)
        if (!existing.getEmail().equals(request.getEmail())) {
            if (usersRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email уже используется");
            }
            existing.setEmail(request.getEmail());
        }

        // Обновляем остальные поля
        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setCountry(request.getCountry());
        existing.setAvatarUrl(request.getAvatarUrl());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setUpdatedAt(OffsetDateTime.now());

        User updated = usersRepository.save(existing);

        // Генерируем новый JWT с актуальными данными
        String newToken = jwtUtils.generateToken(updated);

        UserResponseDTO dto = toDTO(updated);
        dto.setToken(newToken);
        return dto;
    }

    // === Удаление пользователя ===
    public void deleteUser(Long id) {
        User existing = usersRepository.findById(id).orElse(null);

        if (existing == null) {
            return;
        }

        existing.setIs_delete(Boolean.TRUE);

        usersRepository.save(existing);
    }

    // === Получить текущего пользователя по токену ===
    public UserResponseDTO showMe(String token) {
        // 1. Убираем префикс "Bearer ", если он есть (обычно фронтенд присылает его так)
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 2. Извлекаем ID из токена с помощью твоего JwtUtils
            Long userId = jwtUtils.extractUserId(token);
            log.info("Извлечен userId {} из токена", userId);

            // 3. Ищем пользователя в базе и преобразуем в DTO
            return usersRepository.findById(userId)
                    .map(this::toDTO)
                    .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        } catch (Exception e) {
            log.error("Ошибка при обработке токена: {}", e.getMessage());
            return null; // Или выбрось свое исключение для обработки в ControllerAdvice
        }
    }

    public void changePassword(String token, ChangePasswordRequestDTO request) {
        if (request.getOldPassword() == null || request.getNewPassword() == null) {
            throw new RuntimeException("Пароли не могут быть пустыми");
        }

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtils.extractUserId(token);
        log.info("Смена пароля для userId {}", userId);

        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверяем старый пароль
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Неверный старый пароль");
        }

        // Проверяем, что новый пароль не совпадает со старым
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("Новый пароль должен отличаться от старого");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(OffsetDateTime.now());
        usersRepository.save(user);

        log.info("Пароль для пользователя {} успешно обновлён", user.getUsername());
    }

    // === Преобразование сущности в DTO ===
    private UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .country(user.getCountry())
                .avatarUrl(user.getAvatarUrl())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .dateOfBirth(user.getDateOfBirth())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .is_delete(user.getIs_delete())
                .build();
    }
}
