package ru.safiullina.dwCloudService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import ru.safiullina.dwCloudService.security.jwt.TokenAuthenticationProvider;
import ru.safiullina.dwCloudService.security.login.LoginAuthenticationProvider;

/**
 * AuthenticationManager не выполняет аутентификацию,
 * он служит контейнером для провайдеров, которые выполняют эту задачу.
 * Интерфейс AuthenticationManager берет на себя ответственность за поиск подходящего провайдера
 * и передачу ему запроса.
 * Здесь у нас создание менеджера и регистрация провайдеров.
 */
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
