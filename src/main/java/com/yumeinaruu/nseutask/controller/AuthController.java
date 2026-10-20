package com.yumeinaruu.nseutask.controller;

import com.yumeinaruu.nseutask.docs.ApiValidationError;
import com.yumeinaruu.nseutask.dto.AuthResponse;
import com.yumeinaruu.nseutask.dto.ErrorResponse;
import com.yumeinaruu.nseutask.dto.LoginRequest;
import com.yumeinaruu.nseutask.dto.RegisterRequest;
import com.yumeinaruu.nseutask.dto.UserResponse;
import com.yumeinaruu.nseutask.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Public endpoints: registration and login")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a user with the USER role."
    )
    @ApiResponse(responseCode = "201", description = "User registered")
    @ApiResponse(
            responseCode = "409",
            description = "Username is already taken (code 1002) or email is already registered (code 1003)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiValidationError
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest request) {
        return authService.registerUser(request);
    }

    @Operation(
            summary = "Log in and get a JWT",
            description = "Returns a token that contains the username, the role and the expiration time "
                    + "(24 hours by default). Send it as 'Authorization: Bearer <token>'. "
                    + "A wrong password and an unknown user give the same error."
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(
            responseCode = "401",
            description = "Invalid username or password (code 1004)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiValidationError
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse loginUser(@Valid @RequestBody LoginRequest request) {
        return authService.loginUser(request);
    }

}
