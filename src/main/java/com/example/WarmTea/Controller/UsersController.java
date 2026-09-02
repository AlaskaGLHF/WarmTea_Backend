package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.ChangePasswordRequestDTO;
import com.example.WarmTea.Dtos.UsersDto;

import com.example.WarmTea.Service.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UsersService usersService;
    private final ObjectMapper objectMapper;

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
        // Вызываем сервис для выполнения операции удаления
        usersService.deleteUser(id);

        // Возвращаем статус 204 No Content, как ожидается для успешного DELETE
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(
            summary = "Получить пользователя по токену",
            description = "Возвращает данные пользователя по его токену"
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
    public ResponseEntity<UsersDto.UserResponseDTO> getUser(
            @Parameter(description = "Токен пользователя для поиска", example = "")
            @RequestParam String token
    ) {
        UsersDto.UserResponseDTO user = usersService.showMe(token);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) throws IOException {
        // Читаем тело запроса
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String body = sb.toString();

        // Парсим JSON в DTO вручную
        ObjectMapper mapper = new ObjectMapper(); // или внедрите бин
        ChangePasswordRequestDTO dto = mapper.readValue(body, ChangePasswordRequestDTO.class);

        log.info("DTO: {}", dto);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Отсутствует или некорректный заголовок Authorization");
        }
        String token = authHeader.substring(7);
        usersService.changePassword(token, dto);
        return ResponseEntity.ok().build();
    }

}
