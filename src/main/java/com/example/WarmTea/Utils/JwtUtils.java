package com.example.WarmTea.Utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Генерация JWT токена с временем жизни 1 час.
     * @param username имя пользователя (subject)
     * @return строка токена
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600_000); // 1 час

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();

        log.debug("Создан JWT токен для пользователя: {}", username);
        return token;
    }

    /**
     * Проверка подлинности и срока действия токена.
     * @param token JWT токен
     * @return true если токен корректный и не истёк
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()           // ✅ работает в JJWT 0.11+ и 0.12.6
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            log.debug("JWT токен успешно прошёл валидацию");
            return true;
        } catch (Exception e) {
            log.warn("Ошибка валидации JWT токена: {}", e.getMessage());
            return false;
        }
    }
}
