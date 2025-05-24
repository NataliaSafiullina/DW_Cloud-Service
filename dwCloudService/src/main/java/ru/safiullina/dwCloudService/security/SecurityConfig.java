package ru.safiullina.dwCloudService.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.safiullina.dwCloudService.exeption.ErrorResponseHandler;
import ru.safiullina.dwCloudService.security.jwt.JwtTokenProvider;
import ru.safiullina.dwCloudService.security.jwt.TokenAuthenticationFilter;
import ru.safiullina.dwCloudService.security.login.LoginAuthenticationFilter;
import ru.safiullina.dwCloudService.security.matcher.SkipPathRequestMatcher;
import ru.safiullina.dwCloudService.service.UserDetailsServiceImpl;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final String SIGNUP_ENDPOINT = "/cloud/signup";
    private static final String LOGIN_ENDPOINT = "/cloud/login";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationFailureHandler failureHandler;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final ErrorResponseHandler accessDeniedHandler;

    @Autowired
    public final UserDetailsServiceImpl userDetailsService;

    /**
     * Конфигурируем безопасность на уровне endpoint
     *
     * @param http - объект, в котором сконфигурируем ограничения доступа.
     * @return какой способ аутентификации использовать и для какого endpoint
     */
    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        // SessionCreationPolicy.STATELESS - SpringSSecurity не создаст сессию HttpSession и
        // не будет использовать его для получения SecurityContext.
        // SessionCreationPolicy.ALWAYS - создастся сессия HttpSession.
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(configurer -> configurer
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SIGNUP_ENDPOINT).permitAll()
                        .requestMatchers(LOGIN_ENDPOINT).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(buildLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(buildTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout.logoutUrl("/clod/logout").clearAuthentication(true).permitAll());
        return http.build();
    }

    /**
     * Фильтр токенов, который перехватывает все запросы и проверяет токен внутри запроса.
     * Исключаем эту проверку для запроса на регистрацию и вход,
     * для этого надо задать механизм для опознавания путей, с которым этот фильтр будет работать при создании фильтра токена.
     *
     * @return фильтр
     */

    @Bean
    protected TokenAuthenticationFilter buildTokenAuthenticationFilter() {
        List<String> pathsToSkip = new ArrayList<>(Arrays.asList(LOGIN_ENDPOINT, SIGNUP_ENDPOINT));
        SkipPathRequestMatcher matcher = new SkipPathRequestMatcher(pathsToSkip);
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(jwtTokenProvider, matcher, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    protected LoginAuthenticationFilter buildLoginProcessingFilter() {
        LoginAuthenticationFilter filter = new LoginAuthenticationFilter(LOGIN_ENDPOINT,
                authenticationSuccessHandler, failureHandler);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Метод кодировщик паролей, который делегирует полномочия другому кодировщику паролей
     * на основе префиксного идентификатора, например {bcrypt}.
     *
     * @return the PasswordEncoder to use
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
