package dev.guilhermeluan.todo_list.controller;

import dev.guilhermeluan.todo_list.dto.TaskPostRequest;
import dev.guilhermeluan.todo_list.dto.TaskPostResponse;
import dev.guilhermeluan.todo_list.dto.UpdateTaskStatusRequest;
import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskMapper;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("v1/tasks")
public class TaskController {
    private final TaskService service;
    private final TaskMapper mapper;

    public TaskController(TaskService service, TaskMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<TaskPostResponse> create(@RequestBody @Valid TaskPostRequest request) {
        Task taskToSave = mapper.toTask(request);
        Task taskSaved = service.save(taskToSave);

        TaskPostResponse response = mapper.toTaskPostResponse(taskSaved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<dev.guilhermeluan.dtos.TaskGetResponse>> listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            Pageable pageable
    ) {
        Page<Task> tasksPage = service.findAll(status, priority, dueDate, pageable);
        Page<dev.guilhermeluan.dtos.TaskGetResponse> tasksResponsePage = tasksPage.map(mapper::toTaskResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(tasksResponsePage);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@RequestBody @Valid UpdateTaskStatusRequest request, @PathVariable Long id) {
        Task taskToUpdate = mapper.toTask(request);
        service.updateStatus(taskToUpdate.getStatus(), id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
