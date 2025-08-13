package dev.guilhermeluan.todo_list.controller;

import dev.guilhermeluan.todo_list.dto.AuthenticationDTO;
import dev.guilhermeluan.todo_list.dto.LoginResponseDTO;
import dev.guilhermeluan.todo_list.dto.RegisterDTO;
import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.repository.TaskRepository;
import dev.guilhermeluan.todo_list.repository.UserRepository;
import dev.guilhermeluan.todo_list.service.AuthenticationService;
import dev.guilhermeluan.todo_list.utils.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class)
@ComponentScan(basePackages = "dev.guilhermeluan")
class AuthenticationControllerTest {
    private static final String BASE_URL = "/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileUtils fileUtils;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private TaskRepository taskRepository;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /auth/login returns JWT token when credentials are valid")
    void login_ReturnsToken_WhenCredentialsAreValid() throws Exception {
        var loginResponse = new LoginResponseDTO("jwt-token-123");
        BDDMockito.when(authenticationService.authenticate(ArgumentMatchers.any(AuthenticationDTO.class)))
                .thenReturn(loginResponse);

        var request = fileUtils.readResourceFile("auth/post-request-login-200.json");

        mockMvc.perform(post(BASE_URL + "/login")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    @DisplayName("POST /auth/login returns 400 when request body is invalid")
    void login_Returns400_WhenRequestBodyIsInvalid() throws Exception {
        var request = fileUtils.readResourceFile("auth/post-request-login-400.json");

        BDDMockito.when(authenticationService.authenticate(ArgumentMatchers.any(AuthenticationDTO.class)))
                .thenThrow(BadRequestException.class);

        mockMvc.perform(post(BASE_URL + "/login")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register creates user when data is valid")
    void register_CreatesUser_WhenDataIsValid() throws Exception {
        BDDMockito.doNothing().when(authenticationService).register(ArgumentMatchers.any(RegisterDTO.class));

        var request = fileUtils.readResourceFile("auth/post-request-register-201.json");

        mockMvc.perform(post(BASE_URL + "/register")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /auth/register returns 400 when username already exists")
    void register_Returns400_WhenUsernameAlreadyExists() throws Exception {
        BDDMockito.doThrow(new BadRequestException("Usuário já existe com o login: existinguser"))
                .when(authenticationService).register(ArgumentMatchers.any(RegisterDTO.class));

        var request = fileUtils.readResourceFile("auth/post-request-register-400.json");

        mockMvc.perform(post(BASE_URL + "/register")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register creates admin user when role is ADMIN")
    void register_CreatesAdminUser_WhenRoleIsAdmin() throws Exception {
        BDDMockito.doNothing().when(authenticationService).register(ArgumentMatchers.any(RegisterDTO.class));

        var request = fileUtils.readResourceFile("auth/post-request-register-admin-201.json");

        mockMvc.perform(post(BASE_URL + "/register")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }
}
