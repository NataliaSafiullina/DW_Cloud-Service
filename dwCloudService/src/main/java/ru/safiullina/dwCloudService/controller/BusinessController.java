package ru.safiullina.dwCloudService.controller;

import org.springframework.web.bind.annotation.*;
import ru.safiullina.dwCloudService.security.jwt.JwtTokenProvider;
import ru.safiullina.dwCloudService.service.JwtService;
import ru.safiullina.dwCloudService.service.UserService;


@RestController
@RequestMapping("/cloud")
public class BusinessController {

    private final UserService userService;
    private final JwtService jwtService;

    public BusinessController(UserService userService, JwtTokenProvider jwtTokenProvider, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/count")
    public Long getUsersCount(@RequestHeader("auth-token") String authToken) {
        System.out.println("5 +++ Token from header = " + authToken);
        System.out.println("51 +++ Парсер из сервиса = " + jwtService.extractUsernameFromToken(authToken));
        return userService.getUsersCount();
    }


//    @PostMapping("/logout")
//    public String performLogout() {
//        // .. perform logout
//        return "redirect:/logout";
//    }

}
