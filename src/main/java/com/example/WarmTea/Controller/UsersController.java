package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.UsersDto;
import com.example.WarmTea.Dtos.UsersDto.UserRequestDTO;
import com.example.WarmTea.Dtos.UsersDto.UserResponseDTO;
import com.example.WarmTea.Service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    // === Получить всех пользователей ===
    @GetMapping
    @Operation(
            summary = "Получить список всех пользователей",
            description = "Возвращает список всех зарегистрированных пользователей"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список успешно получен",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UsersDto.UserResponseDTO.class))
    )
    public ResponseEntity<List<UsersDto.UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(usersService.getAllUsers());
    }

    // === Получить пользователя по ID ===
    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе по его уникальному идентификатору"
    )
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<UsersDto.UserResponseDTO> getUserById(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(usersService.getUserById(id));
    }

    // === Получить пользователя по email ===
    @GetMapping("/by-email")
    @Operation(
            summary = "Получить пользователя по email",
            description = "Возвращает данные пользователя по его email"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Пользователь найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UsersDto.UserResponseDTO.class)
            )
    )
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<UsersDto.UserResponseDTO> getUserByEmail(
            @Parameter(description = "Email пользователя для поиска", example = "user@example.com")
            @RequestParam String email
    ) {
        UsersDto.UserResponseDTO user = usersService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // === Обновить данные пользователя ===
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить данные пользователя",
            description = "Позволяет изменить информацию о пользователе по его ID"
    )
    @ApiResponse(responseCode = "200", description = "Данные успешно обновлены")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @RequestBody(
            description = "Обновляемые данные пользователя",
            required = true,
            content = @Content(schema = @Schema(implementation = UsersDto.UserRequestDTO.class))
    )
    public ResponseEntity<UsersDto.UserResponseDTO> updateUser(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody UsersDto.UserRequestDTO dto
    ) {
        return ResponseEntity.ok(usersService.updateUser(id, dto));
    }

    // === Удалить пользователя ===
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID")
    @ApiResponse(responseCode = "204", description = "Пользователь успешно удалён")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id
    ) {
        usersService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
