package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.UsersDto.UserRequestDTO;
import com.example.WarmTea.Dtos.UsersDto.UserResponseDTO;
import com.example.WarmTea.Models.User;
import com.example.WarmTea.Repository.UsersRepository;
import com.example.WarmTea.Utils.JwtUtils;
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
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User existing = usersRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        existing.setEmail(request.getEmail());
        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setCountry(request.getCountry());
        existing.setAvatarUrl(request.getAvatarUrl());
        existing.setDateOfBirth(request.getDateOfBirth());
        existing.setUpdatedAt(OffsetDateTime.now());

        User updated = usersRepository.save(existing);
        return toDTO(updated);
    }

    // === Удаление пользователя ===
    public boolean deleteUser(Long id) {
        if (!usersRepository.existsById(id)) {
            return false;
        }
        usersRepository.deleteById(id);
        return true;
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
                .build();
    }
}
