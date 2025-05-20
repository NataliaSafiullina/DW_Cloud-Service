package ru.safiullina.dwCloudService.security;

import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.safiullina.dwCloudService.security.jwt.TokenAuthenticationProvider;
import ru.safiullina.dwCloudService.security.login.LoginAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;


@Configuration
public class AuthenticationManagerConfiguration {

    private final TokenAuthenticationProvider tokenAuthenticationProvider;

    private final LoginAuthenticationProvider loginAuthenticationProvider;

    public AuthenticationManagerConfiguration(final TokenAuthenticationProvider tokenAuthenticationProvider,
                                              final LoginAuthenticationProvider loginAuthenticationProvider) {
        this.tokenAuthenticationProvider = tokenAuthenticationProvider;
        this.loginAuthenticationProvider = loginAuthenticationProvider;

    }

    @Bean
    public AuthenticationManager authenticationManager(final ObjectPostProcessor<Object> objectPostProcessor) throws Exception {
        var auth = new AuthenticationManagerBuilder(objectPostProcessor);
        auth.authenticationProvider(loginAuthenticationProvider);
        auth.authenticationProvider(tokenAuthenticationProvider);

        return auth.build();
    }

}
