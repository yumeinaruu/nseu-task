package com.yumeinaruu.nseutask.service;

import com.yumeinaruu.nseutask.dto.AuthResponse;
import com.yumeinaruu.nseutask.dto.LoginRequest;
import com.yumeinaruu.nseutask.dto.RegisterRequest;
import com.yumeinaruu.nseutask.dto.UserResponse;

public interface AuthService {

    UserResponse registerUser(RegisterRequest request);

    AuthResponse loginUser(LoginRequest request);

}
