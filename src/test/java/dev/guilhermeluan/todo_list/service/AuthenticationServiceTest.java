package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.dto.AuthenticationDTO;
import dev.guilhermeluan.todo_list.dto.LoginResponseDTO;
import dev.guilhermeluan.todo_list.dto.RegisterDTO;
import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.infra.security.TokenService;
import dev.guilhermeluan.todo_list.model.User;
import dev.guilhermeluan.todo_list.model.UserRole;
import dev.guilhermeluan.todo_list.repository.UserRepository;
import dev.guilhermeluan.todo_list.utils.UserUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private Authentication authentication;

    private UserUtils userUtils = new UserUtils();
    private User testUser;
    private AuthenticationDTO authData;
    private RegisterDTO registerData;

    @BeforeEach
    void setUp() {
        testUser = userUtils.newUserTest();
        authData = new AuthenticationDTO(testUser.getUsername(), testUser.getPassword());
        registerData = new RegisterDTO(testUser.getUsername(), testUser.getPassword(), testUser.getRole());
    }

    @Test
    @DisplayName("authenticate returns LoginResponseDTO when credentials are valid")
    void authenticate_ReturnsJWTToken_WhenCredentialsAreValid() {
        String expectedToken = "jwt-token-123";
        var usernamePasswordToken = new UsernamePasswordAuthenticationToken(
                authData.login(), authData.password());

        BDDMockito.when(authenticationManager.authenticate(usernamePasswordToken))
                .thenReturn(authentication);
        BDDMockito.when(authentication.getPrincipal()).thenReturn(testUser);
        BDDMockito.when(tokenService.generateToken(testUser)).thenReturn(expectedToken);

        LoginResponseDTO result = authenticationService.authenticate(authData);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.token()).isEqualTo(expectedToken);

        Mockito.verify(authenticationManager, Mockito.times(1)).authenticate(usernamePasswordToken);
        Mockito.verify(tokenService, Mockito.times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("register creates new user when username does not exist")
    void register_CreatesNewUser_WhenUsernameDoesNotExist() {
        BDDMockito.when(userRepository.findByLogin(registerData.login())).thenReturn(null);
        BDDMockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(testUser);

        Assertions.assertThatNoException().isThrownBy(
                () -> authenticationService.register(registerData)
        );

        Mockito.verify(userRepository, Mockito.times(1)).findByLogin(registerData.login());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    @DisplayName("register throws BadRequestException when username already exists")
    void register_ThrowsBadRequestException_WhenUsernameAlreadyExists() {
        BDDMockito.when(userRepository.findByLogin(registerData.login())).thenReturn(testUser);

        Assertions.assertThatException().isThrownBy(
                        () -> authenticationService.register(registerData)
                ).isInstanceOf(BadRequestException.class);

        Mockito.verify(userRepository, Mockito.times(1)).findByLogin(registerData.login());
        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any(User.class));
    }

    @Test
    @DisplayName("register encrypts password correctly")
    void register_EncryptsPasswordCorrectly_WhenCreatingUser() {
        BDDMockito.when(userRepository.findByLogin(registerData.login())).thenReturn(null);

        authenticationService.register(registerData);

        Mockito.verify(userRepository).save(Mockito.argThat(user -> {
            return !user.getPassword().equals(registerData.password()) &&
                   user.getPassword().length() > 50;
        }));
    }

    @Test
    @DisplayName("register creates user with correct role")
    void register_CreatesUserWithCorrectRole_WhenCalled() {
        var adminRegisterData = new RegisterDTO("admin", "password123", UserRole.ADMIN);
        BDDMockito.when(userRepository.findByLogin(adminRegisterData.login())).thenReturn(null);

        authenticationService.register(adminRegisterData);

        Mockito.verify(userRepository).save(Mockito.argThat(user ->
                user.getUsername().equals(adminRegisterData.login()) &&
                user.getRole().equals(UserRole.ADMIN)
        ));
    }
}
