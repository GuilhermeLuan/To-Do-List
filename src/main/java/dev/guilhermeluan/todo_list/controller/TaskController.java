package dev.guilhermeluan.todo_list.controller;

import dev.guilhermeluan.todo_list.dto.*;
import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskMapper;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.service.TaskService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
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

    @PostMapping({"/{parentId}/subtasks"})
    public ResponseEntity<TaskPostResponse> createSubTask(
            @RequestBody @Valid TaskPostRequest request,
            @PathVariable("parentId") Long parentId) {

        Task subTaskToSave = mapper.toTask(request);
        Task subTaskSaved = service.createSubTask(parentId, subTaskToSave);

        TaskPostResponse response = mapper.toTaskPostResponse(subTaskSaved);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TaskGetResponse>> listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            Pageable pageable
    ) {
        Page<Task> tasksPage = service.findAll(status, priority, dueDate, pageable);
        Page<TaskGetResponse> tasksResponsePage = tasksPage.map(mapper::toTaskResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(tasksResponsePage);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@RequestBody @Valid UpdateTaskStatusRequest request, @PathVariable Long id) {
        Task taskToUpdate = mapper.toTask(request);
        service.updateStatus(taskToUpdate.getStatus(), id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

//    @PatchMapping("/{id}")
//    public ResponseEntity<Void> updateTask(@RequestBody @Valid TaskPutRequest request, @PathVariable Long id) {
//        Task taskToUpdate = mapper.toTask(request);
//        taskToUpdate.setId(id);
//        service.update(taskToUpdate);
//
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
