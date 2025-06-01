package ru.safiullina.dwCloudService.exeption;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.service.spi.ServiceException;
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
import ru.safiullina.dwCloudService.dto.ErrorMessageResponse;
import ru.safiullina.dwCloudService.security.exeption.ExpiredTokenException;
import ru.safiullina.dwCloudService.utils.JsonUtils;
import ru.safiullina.dwCloudService.utils.ResponseText;

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
        switch (exception) {
            case AuthenticationException authenticationException ->
                    handleAuthenticationException(authenticationException, response);
            case ServiceException serviceException -> handleServiceException(serviceException, response);
            case ErrorInputDataException errorInputDataException ->
                    handleErrorInputDataException(errorInputDataException, response);
            default -> handleInternalServerError(exception, response);
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

    private static void handleErrorInputDataException(ErrorInputDataException exception, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
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
                    new ErrorMessageResponse(ResponseText.UNAUTHORIZED_ERROR, HttpStatus.UNAUTHORIZED.value()));
        }
        if (authenticationException instanceof BadCredentialsException || authenticationException instanceof UsernameNotFoundException) {
            JsonUtils.writeValue(getWriter(response),
                    new ErrorMessageResponse(ResponseText.UNAUTHORIZED_ERROR, HttpStatus.UNAUTHORIZED.value()));
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            JsonUtils.writeValue(getWriter(response),
                    new ErrorMessageResponse(ResponseText.BAD_CREDENTIALS, HttpStatus.BAD_REQUEST.value()));
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