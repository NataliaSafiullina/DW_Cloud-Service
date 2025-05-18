package ru.safiullina.dwCloudService.security.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
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

        System.out.println("6 +++ " + authentication.isAuthenticated());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println("7 +++ " + userDetails.toString());

        JwtPair jwtPair = tokenProvider.generateTokenPair(userDetails);
        System.out.println("8 +++ " + jwtPair.toString());

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        System.out.println("9 +++ " + response.getStatus() + "  " + response.toString());

        //TODO: сделать возврат ответа d нужном формате LoginResponse
        JsonUtils.writeValue(response.getWriter(), jwtPair);

    }
}
