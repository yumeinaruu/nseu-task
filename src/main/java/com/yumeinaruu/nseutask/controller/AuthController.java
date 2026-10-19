package com.yumeinaruu.nseutask.controller;

import com.yumeinaruu.nseutask.dto.AuthResponse;
import com.yumeinaruu.nseutask.dto.LoginRequest;
import com.yumeinaruu.nseutask.dto.RegisterRequest;
import com.yumeinaruu.nseutask.dto.UserResponse;
import com.yumeinaruu.nseutask.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest request) {
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse loginUser(@Valid @RequestBody LoginRequest request) {
        return authService.loginUser(request);
    }

}
