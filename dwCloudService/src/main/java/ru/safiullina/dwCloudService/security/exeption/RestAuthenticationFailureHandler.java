package ru.safiullina.dwCloudService.security.exeption;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import ru.safiullina.dwCloudService.exeption.ErrorResponseHandler;

@Component
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ErrorResponseHandler errorResponseHandler;

    @Autowired
    public RestAuthenticationFailureHandler(final ErrorResponseHandler errorResponseHandler) {
        this.errorResponseHandler = errorResponseHandler;
    }

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final AuthenticationException e) {
        errorResponseHandler.handle(e, response);
    }
}
