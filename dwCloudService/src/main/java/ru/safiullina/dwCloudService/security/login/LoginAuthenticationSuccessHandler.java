package ru.safiullina.dwCloudService.security.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.safiullina.dwCloudService.dto.LoginResponse;
import ru.safiullina.dwCloudService.security.jwt.JwtPair;
import ru.safiullina.dwCloudService.security.jwt.JwtTokenProvider;
import ru.safiullina.dwCloudService.utils.JsonUtils;

import java.io.IOException;


@Component(value = "loginAuthenticationSuccessHandler")
public class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    public LoginAuthenticationSuccessHandler(final JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        JwtPair jwtPair = tokenProvider.generateTokenPair(userDetails);

        System.out.println("3 +++ Token = " + jwtPair.getToken());

        // Формируем ответ в нужном формате LoginResponse
        // Media type = application/json
        // { "auth-token" : "string" }
        // Изменение стиля обеспечивается за счет аннотации @JsonProperty("auth-token")
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        LoginResponse loginResponse = new LoginResponse(jwtPair.getToken());

        JsonUtils.writeValue(response.getWriter(), loginResponse);

    }
}
