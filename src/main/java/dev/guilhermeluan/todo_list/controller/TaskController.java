package dev.guilhermeluan.todo_list.controller;

import dev.guilhermeluan.todo_list.dto.*;
import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskMapper;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("v1/tasks")
@Tag(name = "Tarefas", description = "API para gerenciamento de tarefas e subtarefas")
public class TaskController {
    private final TaskService service;
    private final TaskMapper mapper;

    public TaskController(TaskService service, TaskMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(
        summary = "Criar nova tarefa",
        description = "Cria uma nova tarefa principal no sistema. Todas as tarefas criadas iniciam como tarefas principais (não são subtarefas)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tarefa criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskPostResponse.class),
                examples = @ExampleObject(
                    name = "Exemplo de resposta",
                    value = """
                    {
                      "id": 1,
                      "title": "Desenvolver API de autenticação",
                      "description": "Implementar autenticação JWT com rotas protegidas, conforme item opcional na especificação.",
                      "dueDate": "2025-08-18T23:59:00-03:00",
                      "status": "TO_DO",
                      "priority": "HIGH",
                      "isSubTask": false,
                      "subTasks": []
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TaskPostResponse> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da tarefa a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskPostRequest.class),
                examples = @ExampleObject(
                    name = "Exemplo de requisição",
                    value = """
                    {
                      "title": "Desenvolver API de autenticação",
                      "description": "Implementar autenticação JWT com rotas protegidas, conforme item opcional na especificação.",
                      "dueDate": "2025-08-18T23:59:00-03:00",
                      "status": "TO_DO",
                      "priority": "HIGH"
                    }
                    """
                )
            )
        )
        @RequestBody @Valid TaskPostRequest request) {
        Task taskToSave = mapper.toTask(request);
        Task taskSaved = service.save(taskToSave);

        TaskPostResponse response = mapper.toTaskPostResponse(taskSaved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping({"/{parentId}/subtasks"})
    @Operation(
        summary = "Criar nova subtarefa",
        description = "Cria uma nova subtarefa vinculada a uma tarefa principal existente. Não é possível criar subtarefas de outras subtarefas (máximo 2 níveis de hierarquia)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Subtarefa criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskPostResponse.class),
                examples = @ExampleObject(
                    name = "Exemplo de resposta",
                    value = """
                    {
                      "id": 2,
                      "title": "Criar endpoint de login",
                      "description": "O endpoint deve receber email/senha e retornar um token JWT válido.",
                      "status": "TO_DO",
                      "priority": "HIGH",
                      "isSubTask": true,
                      "parentTaskId": 1
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Tentativa de criar subtarefa de uma subtarefa ou dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Tarefa pai não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TaskPostResponse> createSubTask(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da subtarefa a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskPostRequest.class),
                examples = @ExampleObject(
                    name = "Exemplo de requisição",
                    value = """
                    {
                      "title": "Criar endpoint de login",
                      "description": "O endpoint deve receber email/senha e retornar um token JWT válido.",
                      "status": "TO_DO",
                      "priority": "HIGH"
                    }
                    """
                )
            )
        )
        @RequestBody @Valid TaskPostRequest request,
        @Parameter(description = "ID da tarefa pai", required = true, example = "1")
        @PathVariable("parentId") Long parentId) {

        Task subTaskToSave = mapper.toTask(request);
        Task subTaskSaved = service.createSubTask(parentId, subTaskToSave);

        TaskPostResponse response = mapper.toTaskPostResponse(subTaskSaved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Listar tarefas",
        description = "Retorna uma lista paginada de tarefas com filtros opcionais por status, prioridade e data de vencimento. Suporta ordenação por qualquer campo."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tarefas retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    name = "Exemplo de resposta paginada",
                    value = """
                    {
                      "content": [
                        {
                          "id": 1,
                          "title": "Desenvolver API de autenticação",
                          "description": "Implementar autenticação JWT com rotas protegidas",
                          "dueDate": "2025-08-18T23:59:00-03:00",
                          "status": "TO_DO",
                          "priority": "HIGH",
                          "isSubTask": false,
                          "subTasks": [
                            {
                              "id": 2,
                              "title": "Criar endpoint de login",
                              "status": "TO_DO"
                            }
                          ]
                        }
                      ],
                      "pageable": {
                        "pageNumber": 0,
                        "pageSize": 10,
                        "sort": {
                          "sorted": true,
                          "empty": false
                        }
                      },
                      "totalElements": 1,
                      "totalPages": 1,
                      "first": true,
                      "last": true,
                      "size": 10,
                      "number": 0,
                      "numberOfElements": 1,
                      "empty": false
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Page<TaskGetResponse>> listTasks(
        @Parameter(description = "Filtrar por status da tarefa", example = "TO_DO")
        @RequestParam(required = false) TaskStatus status,
        @Parameter(description = "Filtrar por prioridade da tarefa", example = "HIGH")
        @RequestParam(required = false) Priority priority,
        @Parameter(description = "Filtrar por data de vencimento (formato: YYYY-MM-DD)", example = "2025-08-18")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
        @Parameter(description = "Número da página (começa em 0)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamanho da página", example = "10")
        @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "Campo para ordenação (ex: title, dueDate, status, priority)", example = "title")
        @RequestParam(defaultValue = "id") String sort,
        @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "ASC")
        @RequestParam(defaultValue = "ASC") String direction
    ) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

            Page<Task> tasksPage = service.findAll(status, priority, dueDate, pageable);
            Page<TaskGetResponse> tasksResponsePage = tasksPage.map(mapper::toTaskResponseDTO);

            return ResponseEntity.status(HttpStatus.OK).body(tasksResponsePage);
        } catch (IllegalArgumentException e) {
            Pageable defaultPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
            Page<Task> tasksPage = service.findAll(status, priority, dueDate, defaultPageable);
            Page<TaskGetResponse> tasksResponsePage = tasksPage.map(mapper::toTaskResponseDTO);

            return ResponseEntity.status(HttpStatus.OK).body(tasksResponsePage);
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Atualizar status da tarefa",
        description = "Atualiza apenas o status de uma tarefa específica. Para tarefas principais, todas as subtarefas devem estar concluídas antes de marcar como 'DONE'."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Tentativa de finalizar tarefa com subtarefas pendentes"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> updateStatus(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novo status da tarefa",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UpdateTaskStatusRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Marcar como em progresso",
                        value = """
                        {
                          "status": "IN_PROGRESS"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Marcar como concluída",
                        value = """
                        {
                          "status": "DONE"
                        }
                        """
                    )
                }
            )
        )
        @RequestBody @Valid UpdateTaskStatusRequest request,
        @Parameter(description = "ID da tarefa", required = true, example = "1")
        @PathVariable Long id) {
        Task taskToUpdate = mapper.toTask(request);
        service.updateStatus(taskToUpdate.getStatus(), id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar tarefa completa",
        description = "Atualiza todos os dados de uma tarefa específica. As subtarefas existentes são preservadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tarefa atualizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> updateTask(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da tarefa",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskPutRequest.class),
                examples = @ExampleObject(
                    name = "Exemplo de atualização",
                    value = """
                    {
                      "title": "Implementar autenticação JWT completa",
                      "description": "Sistema completo de autenticação com tokens JWT e refresh tokens",
                      "status": "IN_PROGRESS",
                      "priority": "HIGH",
                      "dueDate": "2025-12-31T23:59:59Z"
                    }
                    """
                )
            )
        )
        @RequestBody @Valid TaskPutRequest request,
        @Parameter(description = "ID da tarefa", required = true, example = "1")
        @PathVariable Long id) {
        Task taskToUpdate = mapper.toTask(request);
        taskToUpdate.setId(id);

        service.update(taskToUpdate);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir tarefa",
        description = "Exclui uma tarefa específica. Se for uma tarefa principal, todas as suas subtarefas também serão excluídas automaticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "ID da tarefa a ser excluída", required = true, example = "1")
        @PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
