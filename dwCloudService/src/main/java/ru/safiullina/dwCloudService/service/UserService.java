package ru.safiullina.dwCloudService.service;

import ru.safiullina.dwCloudService.exeption.ServiceException;
import ru.safiullina.dwCloudService.repository.UserRepository;
import ru.safiullina.dwCloudService.entity.User;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class UserService {

    public static final String EXCEPTION_USERNAME_EXISTS = "exception.usernameExists";
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User addUser(final User user) throws ServiceException {
        if (existsByUsername(user.getLogin())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, EXCEPTION_USERNAME_EXISTS);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }


    @Transactional
    public boolean existsByUsername(final String name) {
        return userRepository.existsByLogin(name);
    }


    public User findByUsername(String username) throws ServiceException {
        return userRepository.findByLogin(username)
                .orElseThrow(() -> new ServiceException(HttpStatus.BAD_REQUEST, "exception.user.notFound"));
    }


    public Long getUsersCount() {
        return userRepository.count();
    }
}
