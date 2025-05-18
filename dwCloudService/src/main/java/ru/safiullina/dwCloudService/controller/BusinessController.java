package ru.safiullina.dwCloudService.controller;

import org.springframework.web.bind.annotation.*;
import ru.safiullina.dwCloudService.service.UserService;

@RestController
@RequestMapping("/cloud")
public class BusinessController {

    private final UserService userService;

    public BusinessController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/count")
    public Long getUsersCount(@RequestHeader("auth-token") String authToken) {
        System.out.println("5 +++ Token from header = " + authToken );
        return userService.getUsersCount();
    }



}
