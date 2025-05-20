package ru.safiullina.dwCloudService.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.safiullina.dwCloudService.security.jwt.JwtTokenProvider;
import ru.safiullina.dwCloudService.service.UserService;

@RestController
@RequestMapping("/cloud")
public class BusinessController {

    private final UserService userService;

    public BusinessController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
    }

    @GetMapping("/count")
    public Long getUsersCount(@RequestHeader("auth-token") String authToken,
                              @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("5 +++ Token from header = " + authToken );
        System.out.println("6 +++ User = " );
        return userService.getUsersCount();
    }


//    @PostMapping("/logout")
//    public String performLogout() {
//        // .. perform logout
//        return "redirect:/logout";
//    }

}
