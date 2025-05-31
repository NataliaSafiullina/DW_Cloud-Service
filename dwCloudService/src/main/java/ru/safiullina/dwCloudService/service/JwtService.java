package ru.safiullina.dwCloudService.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    /**
     * Метод парсит токен и извлекает из него имя пользователя, т.е. логин.
     *
     * @param authToken - токен полученный в запросе
     * @return имя зашедшего пользователя
     */
    public String extractUsernameFromToken(String authToken) {

        if (authToken == null) {
            return null;
        }

        // Вырезаем Bearer из токена
        String token = authToken.substring(7);

        // Парсим токен, в токене зашита информация примерно вот так:
        // {"header": {"typ": "JWT", "alg": "HS256"},
        //  "payload": {
        //    "sub": "username",
        //    "exp": 1700000000,
        //    "iat": 1690000000
        //  },
        //  "signature": "...зашифрованная подпись..."}
        // Получаем имя пользователя, т.е. login.
        // subject — это username по стандарту JWT
        return Jwts
                .parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public String getJwtSecret() {
        return jwtSecret;
    }


}
