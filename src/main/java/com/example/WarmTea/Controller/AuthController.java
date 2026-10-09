package com.example.WarmTea.Controller;

import com.example.WarmTea.Dtos.AuthDto;
import com.example.WarmTea.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Методы для авторизации и регистрации пользователей")
public class AuthController {

    private final AuthService authService;

    // === LOGIN ===
    @PostMapping("/login")
    @Operation(
            summary = "Авторизация пользователя",
            description = "Позволяет авторизоваться пользователю по логину и паролю"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Авторизация успешна",
            content = @Content(schema = @Schema(implementation = AuthDto.LoginResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неверный логин или пароль",
            content = @Content
    )
    @RequestBody(
            description = "Данные для входа пользователя",
            required = true,
            content = @Content(schema = @Schema(implementation = AuthDto.LoginRequestDTO.class))
    )
    public ResponseEntity<AuthDto.LoginResponseDTO> login(
            @org.springframework.web.bind.annotation.RequestBody AuthDto.LoginRequestDTO request
    ) {
        AuthDto.LoginResponseDTO tokenResponse = authService.login(request);
        if (tokenResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(tokenResponse);
    }

    // === REGISTER ===
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Регистрация пользователя",
            description = "Создаёт нового пользователя по JSON-запросу без загрузки файла аватара"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Пользователь успешно зарегистрирован",
            content = @Content(schema = @Schema(implementation = AuthDto.RegisterResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации данных или регистрации",
            content = @Content
    )
    public ResponseEntity<?> register(
            @org.springframework.web.bind.annotation.RequestBody AuthDto.RegisterRequestDTO request
    ) {
        try {
            var response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason() == null ? "Ошибка" : e.getReason()));
        } catch (Exception e) {
            log.error("Ошибка регистрации", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
