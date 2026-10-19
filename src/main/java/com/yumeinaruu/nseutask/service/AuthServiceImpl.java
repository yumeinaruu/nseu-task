package com.yumeinaruu.nseutask.service;

import com.yumeinaruu.nseutask.dto.AuthResponse;
import com.yumeinaruu.nseutask.dto.LoginRequest;
import com.yumeinaruu.nseutask.dto.RegisterRequest;
import com.yumeinaruu.nseutask.dto.UserResponse;
import com.yumeinaruu.nseutask.entity.User;
import com.yumeinaruu.nseutask.entity.enums.Role;
import com.yumeinaruu.nseutask.exception.EmailAlreadyExistsException;
import com.yumeinaruu.nseutask.exception.InvalidCredentialsException;
import com.yumeinaruu.nseutask.exception.UsernameAlreadyExistsException;
import com.yumeinaruu.nseutask.mapper.UserMapper;
import com.yumeinaruu.nseutask.repository.UserRepository;
import com.yumeinaruu.nseutask.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final String USERNAME_CONSTRAINT = "uc_users_username";
    private static final String EMAIL_CONSTRAINT = "uc_users_email";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        log.info("Starting user registration: username={}", request.username());
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        User savedUser = saveNewUser(user);
        log.info("Completed user registration: userId={}", savedUser.getId());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse loginUser(LoginRequest request) {
        log.info("Starting user login: username={}", request.username());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException(exception);
        }
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);
        log.info("Completed user login: userId={}", user.getId());
        return new AuthResponse(jwtService.generateToken(user));
    }

    private User saveNewUser(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw translateDuplicateUserError(exception, user);
        }
    }

    private RuntimeException translateDuplicateUserError(DataIntegrityViolationException exception, User user) {
        if (exception.getCause() instanceof ConstraintViolationException violation
                && violation.getConstraintName() != null) {
            String constraintName = violation.getConstraintName();
            if (constraintName.equalsIgnoreCase(USERNAME_CONSTRAINT)) {
                return new UsernameAlreadyExistsException(user.getUsername());
            }
            if (constraintName.equalsIgnoreCase(EMAIL_CONSTRAINT)) {
                return new EmailAlreadyExistsException();
            }
        }
        return exception;
    }

}
