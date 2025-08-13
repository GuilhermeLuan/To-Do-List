package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.dto.AuthenticationDTO;
import dev.guilhermeluan.todo_list.dto.LoginResponseDTO;
import dev.guilhermeluan.todo_list.dto.RegisterDTO;
import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.infra.security.TokenService;
import dev.guilhermeluan.todo_list.model.User;
import dev.guilhermeluan.todo_list.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            TokenService tokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public LoginResponseDTO authenticate(AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        String token = tokenService.generateToken((User) auth.getPrincipal());

        return new LoginResponseDTO(token);
    }

    public void register(RegisterDTO data) {
        validateUserNotExists(data.login());

        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser = new User(data.login(), encryptedPassword, data.role());

        userRepository.save(newUser);
    }

    private void validateUserNotExists(String login) {
        if (userRepository.findByLogin(login) != null) {
            throw new BadRequestException("Usuário já existe com o login: " + login);
        }
    }
}
