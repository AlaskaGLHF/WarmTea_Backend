package com.example.WarmTea.Service;

import com.example.WarmTea.Dtos.AuthDto;
import com.example.WarmTea.Models.RefreshToken;
import com.example.WarmTea.Models.Roles;
import com.example.WarmTea.Models.User;
import com.example.WarmTea.Repository.RefreshTokenRepository;
import com.example.WarmTea.Repository.UsersRepository;
import com.example.WarmTea.Utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;


@Service
@Slf4j
public class AuthService {

    private static final Roles DEFAULT_ROLE = Roles.builder().id(1L).build();
    private final UsersRepository usersRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final S3Service s3Service;

    @Autowired
    public AuthService(UsersRepository usersRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       S3Service s3Service) {
        this.usersRepository = usersRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.s3Service = s3Service;
    }

    // === Авторизация ===
    public AuthDto.LoginResponseDTO login(AuthDto.LoginRequestDTO request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Логин и пароль обязательны");
        }

        Optional<User> userOpt = usersRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Пользователь не найден");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Неверный пароль");
        }

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthDto.LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // === Регистрация ===
    public AuthDto.RegisterResponseDTO register(AuthDto.RegisterRequestDTO request) {
        if (request.getUsername() == null || request.getPassword() == null || request.getEmail() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Необходимо указать username, email и password");
        }

        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Пользователь с таким username уже существует");
        }
        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Пользователь с таким email уже существует");
        }

        log.info("Register: avatarUrl from DTO = '{}'", request.getAvatarUrl());
        String avatarUrl = request.getAvatarUrl();
        if (avatarUrl != null && avatarUrl.isBlank()) {
            avatarUrl = null;
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .country(request.getCountry())
                .avatarUrl(avatarUrl)
                .dateOfBirth(request.getDateOfBirth())
                .createdAt(OffsetDateTime.now())
                .role(DEFAULT_ROLE)
                .build();

        usersRepository.save(user);

        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthDto.RegisterResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // === Создание и сохранение refresh-токена ===
    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(OffsetDateTime.now().plusDays(30))
                .createdAt(OffsetDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}