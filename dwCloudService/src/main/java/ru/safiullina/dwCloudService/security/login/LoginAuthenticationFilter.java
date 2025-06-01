package ru.safiullina.dwCloudService.security.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import ru.safiullina.dwCloudService.dto.LoginRequest;
import ru.safiullina.dwCloudService.security.exeption.AuthMethodNotSupportedException;
import ru.safiullina.dwCloudService.utils.JsonUtils;

import java.io.IOException;

public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private Logger logger = LoggerFactory.getLogger(LoginAuthenticationFilter.class);
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public LoginAuthenticationFilter(final String defaultFilterProcessesUrl,
                                     final AuthenticationSuccessHandler successHandler,
                                     final AuthenticationFailureHandler failureHandler) {
        super(defaultFilterProcessesUrl);
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request,
                                                final HttpServletResponse response) throws AuthenticationException {

        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication method not supported. Request method: " + request.getMethod());
            }
            throw new AuthMethodNotSupportedException("Authentication method not supported");
        }

        LoginRequest loginDTO;

        try {
            loginDTO = JsonUtils.fromReader(request.getReader(), LoginRequest.class);
            System.out.println("1 +++ Attempt auth for " + ((loginDTO == null) ? " null" : loginDTO.toString()));
        } catch (Exception e) {
            throw new AuthenticationServiceException("Invalid login request payload");
        }

        if (StringUtils.isBlank(loginDTO.getLogin()) || StringUtils.isEmpty(loginDTO.getPassword())) {
            throw new AuthenticationServiceException("Username or Password not provided");
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getLogin(), loginDTO.getPassword());
        token.setDetails(authenticationDetailsSource.buildDetails(request));

        return this.getAuthenticationManager().authenticate(token);
    }

    /**
     * Действия при успешной аутентификации по Логину.
     */
    @Override
    protected void successfulAuthentication(final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("2 +++ isAuthenticated = " + authResult.isAuthenticated());

        // Create empty security context and set authentication.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        // TODO: если не получится получить пришедшего пользователя из других мест,
        //  тут сделать сохранение пары user + token в БД, затирать если пара уже есть
        System.out.println("21 +++ Authentication = " + context.getAuthentication());

        // Save the security context to the repo (This adds it to the HTTP session).
        // Но данные исчезнут при перезапуске приложения.
        securityContextRepository.saveContext(context, request, response);

        this.successHandler.onAuthenticationSuccess(request, response, authResult);
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        System.out.println("2 +++ unsuccessfulAuthentication");
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }
}
