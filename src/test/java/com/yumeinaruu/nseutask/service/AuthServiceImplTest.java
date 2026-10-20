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
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String USERNAME = "alice";
    private static final String EMAIL = "alice@example.com";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String TOKEN = "jwt-token";
    private static final String USERNAME_CONSTRAINT = "uc_users_username";
    private static final String EMAIL_CONSTRAINT = "uc_users_email";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository, new UserMapper(), passwordEncoder, authenticationManager, jwtService
        );
    }

    @Test
    void givenNewUser_whenRegistering_thenSavesUserWithEncodedPasswordAndUserRole() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.registerUser(new RegisterRequest(USERNAME, EMAIL, PASSWORD));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(savedUser.capture());
        assertThat(savedUser.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(savedUser.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(response.username()).isEqualTo(USERNAME);
    }

    @Test
    void givenTakenUsername_whenRegistering_thenThrowsUsernameAlreadyExists() {
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(new RegisterRequest(USERNAME, EMAIL, PASSWORD)))
                .isInstanceOf(UsernameAlreadyExistsException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenTakenEmail_whenRegistering_thenThrowsEmailAlreadyExists() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(new RegisterRequest(USERNAME, EMAIL, PASSWORD)))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenUsernameRaceOnSave_whenRegistering_thenTranslatesToUsernameAlreadyExists() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(constraintViolation(USERNAME_CONSTRAINT));

        assertThatThrownBy(() -> authService.registerUser(new RegisterRequest(USERNAME, EMAIL, PASSWORD)))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void givenEmailRaceOnSave_whenRegistering_thenTranslatesToEmailAlreadyExists() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(constraintViolation(EMAIL_CONSTRAINT));

        assertThatThrownBy(() -> authService.registerUser(new RegisterRequest(USERNAME, EMAIL, PASSWORD)))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void givenValidCredentials_whenLoggingIn_thenReturnsToken() {
        User user = new User();
        user.setId(1L);
        user.setUsername(USERNAME);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        AuthResponse response = authService.loginUser(new LoginRequest(USERNAME, PASSWORD));

        assertThat(response.token()).isEqualTo(TOKEN);
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD));
    }

    @Test
    void givenBadCredentials_whenLoggingIn_thenThrowsInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.loginUser(new LoginRequest(USERNAME, PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(jwtService, never()).generateToken(any());
    }

    private DataIntegrityViolationException constraintViolation(String constraintName) {
        return new DataIntegrityViolationException(
                "duplicate", new ConstraintViolationException("duplicate", null, constraintName)
        );
    }

}
