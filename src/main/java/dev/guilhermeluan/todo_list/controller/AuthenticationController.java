package dev.guilhermeluan.todo_list.controller;

import dev.guilhermeluan.todo_list.dto.AuthenticationDTO;
import dev.guilhermeluan.todo_list.dto.LoginResponseDTO;
import dev.guilhermeluan.todo_list.dto.RegisterDTO;
import dev.guilhermeluan.todo_list.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "API para autenticação e registro de usuários")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Realizar login",
            description = "Autentica um usuário no sistema usando login e senha, retornando um token JWT válido para acesso às rotas protegidas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de resposta de login",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ0b2RvLWxpc3QiLCJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNjQxMjMwNDAwLCJleHAiOjE2NDEzMTY4MDB9.example"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Dados de login inválidos ou campos obrigatórios não preenchidos",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    public ResponseEntity<LoginResponseDTO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de login do usuário",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Login de usuário comum",
                                            value = """
                                                    {
                                                    "login": "novoUsuario",
                                                    "password": "minhasenhasegura123"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @RequestBody @Valid AuthenticationDTO data) {
        LoginResponseDTO response = authenticationService.authenticate(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria uma nova conta de usuário no sistema. Valida se o nome de usuário já existe antes de criar a conta. A senha é automaticamente criptografada."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário registrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nome de usuário já existe ou dados inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Usuário já existe",
                                            value = """
                                                    {
                                                      "timestamp": "2025-08-13T14:30:00Z",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Usuário já existe com o login: usuario123",
                                                      "path": "/auth/register"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<Void> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação da nova conta de usuário",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Registro de usuário comum",
                                            value = """
                                                    {
                                                      "login": "novoUsuario",
                                                      "password": "minhasenhasegura123",
                                                      "role": "USER"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @RequestBody @Valid RegisterDTO data) {
        authenticationService.register(data);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}