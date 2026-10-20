package com.yumeinaruu.nseutask.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String TASKS_PATH = "/api/tasks";
    private static final String TASK_PATH = "/api/tasks/{id}";
    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenAuthenticatedUser_whenCreatingTask_thenReturnsCreatedWithDefaults() throws Exception {
        String token = registerAndLogin().token();

        mockMvc.perform(post(TASKS_PATH)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Write docs"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.assignee").doesNotExist());
    }

    @Test
    void givenExistingTask_whenGettingIt_thenReturnsTask() throws Exception {
        TestUser user = registerAndLogin();
        JsonNode task = createTask(user.token(), "Read me");

        mockMvc.perform(get(TASK_PATH, task.get("id").asLong())
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Read me"))
                .andExpect(jsonPath("$.author.id").value(user.id()));
    }

    @Test
    void givenUnknownTask_whenGettingIt_thenReturnsNotFound() throws Exception {
        String token = registerAndLogin().token();

        mockMvc.perform(get(TASK_PATH, 999999).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(2001));
    }

    @Test
    void givenTasksOfDifferentAuthors_whenFilteringByAuthor_thenReturnsOnlyTheirs() throws Exception {
        TestUser first = registerAndLogin();
        TestUser second = registerAndLogin();
        createTask(first.token(), "First task");
        createTask(second.token(), "Second task");

        mockMvc.perform(get(TASKS_PATH)
                        .param("authorId", String.valueOf(first.id()))
                        .header("Authorization", "Bearer " + first.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("First task"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void givenInvalidStatusFilter_whenListingTasks_thenReturnsBadRequestWithoutInternals() throws Exception {
        String token = registerAndLogin().token();

        mockMvc.perform(get(TASKS_PATH).param("status", "UNKNOWN").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(3002))
                .andExpect(jsonPath("$.message").value("status: invalid value"));
    }

    @Test
    void givenInvalidSortField_whenListingTasks_thenReturnsBadRequest() throws Exception {
        String token = registerAndLogin().token();

        mockMvc.perform(get(TASKS_PATH).param("sort", "nope").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(3002));
    }

    @Test
    void givenAuthor_whenUpdatingTask_thenReturnsUpdatedTaskWithNextVersion() throws Exception {
        TestUser user = registerAndLogin();
        JsonNode task = createTask(user.token(), "Old title");

        mockMvc.perform(put(TASK_PATH, task.get("id").asLong())
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody("New title", "IN_PROGRESS", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void givenStaleVersion_whenUpdatingTask_thenReturnsConcurrentModificationConflict() throws Exception {
        TestUser user = registerAndLogin();
        JsonNode task = createTask(user.token(), "Race");
        long taskId = task.get("id").asLong();
        mockMvc.perform(put(TASK_PATH, taskId)
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody("First edit", "TODO", 0)))
                .andExpect(status().isOk());

        mockMvc.perform(put(TASK_PATH, taskId)
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody("Second edit", "TODO", 0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(2003));
    }

    @Test
    void givenAuthor_whenDeletingTask_thenReturnsNoContentAndTaskIsGone() throws Exception {
        TestUser user = registerAndLogin();
        long taskId = createTask(user.token(), "Delete me").get("id").asLong();

        mockMvc.perform(delete(TASK_PATH, taskId).header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(TASK_PATH, taskId).header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenAnotherUser_whenDeletingTask_thenReturnsForbidden() throws Exception {
        long taskId = createTask(registerAndLogin().token(), "Keep me").get("id").asLong();
        String strangerToken = registerAndLogin().token();

        mockMvc.perform(delete(TASK_PATH, taskId).header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(2002));
    }

    private JsonNode createTask(String token, String title) throws Exception {
        MvcResult result = mockMvc.perform(post(TASKS_PATH)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "%s"}
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private TestUser registerAndLogin() throws Exception {
        String username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult registration = mockMvc.perform(post(REGISTER_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "email": "%s@example.com", "password": "%s"}
                                """.formatted(username, username, PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn();
        long userId = objectMapper.readTree(registration.getResponse().getContentAsString()).get("id").asLong();
        assertThat(userId).isPositive();
        return new TestUser(userId, login(username, PASSWORD));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String updateBody(String title, String status, long version) {
        return """
                {"title": "%s", "status": "%s", "priority": "HIGH", "version": %d}
                """.formatted(title, status, version);
    }

    private record TestUser(
            long id,
            String token
    ) {
    }

}
