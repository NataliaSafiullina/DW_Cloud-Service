package ru.safiullina.dwCloudService.security.jwt;

import lombok.Getter;

@Getter
public class JwtPair {

    private final String token;
    private final String refreshToken;

    public JwtPair(final String token, final String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

}
