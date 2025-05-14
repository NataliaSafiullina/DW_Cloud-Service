package ru.safiullina.dwCloudService.security;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.safiullina.dwCloudService.service.UserDetailsServiceImpl;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

//    @Autowired
//    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//
//    @Autowired
//    private AuthEntryPointJwt unauthorizedHandler;
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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

    @Bean
    public AuthenticationManager authenticationManagerBean (AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }


}
