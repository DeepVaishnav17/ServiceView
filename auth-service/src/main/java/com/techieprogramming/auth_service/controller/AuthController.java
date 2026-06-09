package com.techieprogramming.auth_service.controller;

import com.techieprogramming.auth_service.dto.AuthRequest;
import com.techieprogramming.auth_service.dto.RegisterRequest;
import com.techieprogramming.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        authService.register(request);

        return "User Registered Successfully";
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {

        return authService.login(request);
    }
    @GetMapping("/hello")
public String hello() {
    return "JWT Working";
}
}