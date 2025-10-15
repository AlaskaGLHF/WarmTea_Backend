package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.UsersDto.LoginRequestDTO;
import com.example.WarmTea.Dtos.UsersDto.LoginResponseDTO;
import com.example.WarmTea.Dtos.UsersDto.UserRequestDTO;
import com.example.WarmTea.Dtos.UsersDto.UserResponseDTO;
import com.example.WarmTea.Models.Users;
import com.example.WarmTea.Service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    // 🔹 GET — получить всех пользователей
    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return usersService.getAllUsers()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 🔹 GET — получить по id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        Users user = usersService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(toDTO(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔹 GET — получить по email
    @GetMapping("/by-email")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@RequestParam String email) {
        Users user = usersService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(toDTO(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔹 POST — создать пользователя
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO request) {
        UserResponseDTO createdUser = usersService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // 🔹 POST — логин
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO tokenResponse = usersService.login(request);
        if (tokenResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(tokenResponse);
    }

    // 🔹 PUT — обновить данные пользователя
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequestDTO request,
            @RequestHeader("Authorization") String authHeader) {

        UserResponseDTO updatedUser = usersService.updateUser(id, request);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔹 DELETE — удалить пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (usersService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 🔸 mapper Users -> UserResponseDTO
    private UserResponseDTO toDTO(Users user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreated_at()
        );
    }
}
