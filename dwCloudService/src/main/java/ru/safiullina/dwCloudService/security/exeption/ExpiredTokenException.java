package ru.safiullina.dwCloudService.security.exeption;

import org.springframework.security.core.AuthenticationException;

public class ExpiredTokenException extends AuthenticationException {

    public ExpiredTokenException(final String token, final String msg, final Throwable t) {
        super(msg, t);
    }
}
