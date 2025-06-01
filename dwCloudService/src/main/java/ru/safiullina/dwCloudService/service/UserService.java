package ru.safiullina.dwCloudService.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.safiullina.dwCloudService.entity.User;
import ru.safiullina.dwCloudService.exeption.ErrorInputDataException;
import ru.safiullina.dwCloudService.exeption.ServiceException;
import ru.safiullina.dwCloudService.repository.UserRepository;
import ru.safiullina.dwCloudService.utils.ResponseText;

import java.util.Optional;


@Component
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(JwtService jwtService, final UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }


    public User addUser(final User user) throws ServiceException {
        if (existsByUsername(user.getLogin())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, ResponseText.USERNAME_EXISTS);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    Optional<User> findUserByToken(String token) {
        // Получаем имя пользователя из токена.
        String userName = jwtService.extractUsernameFromToken(token);
        // Получаем объект пользователя по его имени.
        Optional<User> user = userRepository.findByLogin(userName);
        if (user.isEmpty()) {
            // Бросаем исключение и попадаем в итоге в handleErrorInputDataException()
            throw new ErrorInputDataException(ResponseText.ERROR_INPUT_DATA);
        }
        return user;
    }

    @Transactional
    public boolean existsByUsername(final String name) {
        return userRepository.existsByLogin(name);
    }


    public User findByUsername(String username) throws ServiceException {
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, ResponseText.USER_NOT_FOUND));
    }


    public Long getUsersCount() {
        return userRepository.count();
    }
}
