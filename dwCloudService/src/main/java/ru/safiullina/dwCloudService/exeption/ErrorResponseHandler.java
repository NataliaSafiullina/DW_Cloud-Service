package ru.safiullina.dwCloudService.exeption;

import ru.safiullina.dwCloudService.dto.ErrorMessageResponse;
import ru.safiullina.dwCloudService.security.exeption.ExpiredTokenException;
import ru.safiullina.dwCloudService.utils.JsonUtils;

import org.hibernate.service.spi.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.io.PrintWriter;

@RestControllerAdvice
public class ErrorResponseHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(ErrorResponseHandler.class);

    @ExceptionHandler(Exception.class)
    public void handle(final Exception exception, final HttpServletResponse response) {
        logger.debug("Processing exception {}", exception.getMessage(), exception);
        if (response.isCommitted()) {
            return;
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (exception instanceof AuthenticationException) {
            handleAuthenticationException((AuthenticationException) exception, response);
        } else if (exception instanceof ServiceException) {
            handleServiceException((ServiceException) exception, response);
        } else {
            handleInternalServerError(exception, response);
        }
    }

    private static void handleInternalServerError(Exception exception, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        JsonUtils.writeValue(getWriter(response), new ErrorMessageResponse(exception.getMessage(), 0));
    }

    private static void handleServiceException(ServiceException exception, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        JsonUtils.writeValue(getWriter(response), new ErrorMessageResponse(exception.getMessage(), 0));
    }

    private static PrintWriter getWriter(HttpServletResponse response) {
        try {
            return response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleAuthenticationException(final AuthenticationException authenticationException,
                                                      final HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        if (authenticationException instanceof ExpiredTokenException) {
            JsonUtils.writeValue(getWriter(response),
                    new ErrorMessageResponse("exception.tokenExpired", HttpStatus.UNAUTHORIZED.value()));
        }
        if (authenticationException instanceof BadCredentialsException || authenticationException instanceof UsernameNotFoundException) {
            JsonUtils.writeValue(getWriter(response),
                    new ErrorMessageResponse("exception.badCredentials", HttpStatus.UNAUTHORIZED.value()));
        } else {
            JsonUtils.writeValue(getWriter(response),
                    new ErrorMessageResponse("exception.authenticationFailed", HttpStatus.UNAUTHORIZED.value()));
        }
    }

    @Override
    public void handle(final HttpServletRequest request,
                       final HttpServletResponse response,
                       final AccessDeniedException accessDeniedException) {
        if (!response.isCommitted()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            JsonUtils.writeValue(getWriter(response), new ErrorMessageResponse("exception.accessDenied", HttpStatus.FORBIDDEN.value()));
        }
    }
}