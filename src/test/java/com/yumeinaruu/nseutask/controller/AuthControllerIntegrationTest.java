package com.yumeinaruu.nseutask.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenNewUser_whenRegistering_thenReturnsCreatedWithoutPassword() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, username + "@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void givenTakenUsername_whenRegistering_thenReturnsConflictWithUsernameCode() throws Exception {
        String username = uniqueUsername();
        registerUser(username, username + "@example.com");

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, "other-" + username + "@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1002));
    }

    @Test
    void givenTakenEmail_whenRegistering_thenReturnsConflictWithEmailCode() throws Exception {
        String username = uniqueUsername();
        String email = username + "@example.com";
        registerUser(username, email);

        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("other-" + username, email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void givenInvalidBody_whenRegistering_thenReturnsBadRequestWithValidationCode() throws Exception {
        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(3002));
    }

    @Test
    void givenRegisteredUser_whenLoggingIn_thenReturnsToken() throws Exception {
        String username = uniqueUsername();
        registerUser(username, username + "@example.com");

        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void givenWrongPassword_whenLoggingIn_thenReturnsUnauthorizedWithCredentialsCode() throws Exception {
        String username = uniqueUsername();
        registerUser(username, username + "@example.com");

        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1004));
    }

    @Test
    void givenUnknownUser_whenLoggingIn_thenReturnsSameErrorAsWrongPassword() throws Exception {
        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(uniqueUsername(), PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1004));
    }

    @Test
    void givenDefaultAdmin_whenLoggingIn_thenReturnsToken() throws Exception {
        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("admin", "12345678")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void givenNoToken_whenCallingProtectedPath_thenReturnsUnauthorizedWithAuthCode() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(3003));
    }

    private void registerUser(String username, String email) throws Exception {
        mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(username, email)))
                .andExpect(status().isCreated());
    }

    private String uniqueUsername() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String registerBody(String username, String email) {
        return """
                {"username": "%s", "email": "%s", "password": "%s"}
                """.formatted(username, email, PASSWORD);
    }

    private String loginBody(String username, String password) {
        return """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);
    }

}
