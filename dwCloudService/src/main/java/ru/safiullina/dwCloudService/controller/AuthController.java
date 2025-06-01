package ru.safiullina.dwCloudService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.safiullina.dwCloudService.dto.SignupRequest;
import ru.safiullina.dwCloudService.entity.User;
import ru.safiullina.dwCloudService.exeption.ServiceException;
import ru.safiullina.dwCloudService.service.UserService;

@RestController
@RequestMapping("/cloud")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public User registerUser(@RequestBody final SignupRequest signUpRequest) throws ServiceException {
        return userService.addUser(new User(signUpRequest.getLogin(), signUpRequest.getPassword()));
    }

    /**
     * Клиентская сторона должна удалить токен из своего хранилища.
     */
    @PostMapping("logout")
    public ResponseEntity<String> logoutUser(@RequestHeader("auth-token") String authToken) {
        // Тут можно добавлять токен в черный список, но так не правильно
        return ResponseEntity.ok("Success logout");
    }

}
