package ru.safiullina.dwCloudService.security.exeption;

import org.springframework.security.authentication.AuthenticationServiceException;

public class AuthMethodNotSupportedException extends AuthenticationServiceException {
    public AuthMethodNotSupportedException(final String msg) {
        super(msg);
    }
}
