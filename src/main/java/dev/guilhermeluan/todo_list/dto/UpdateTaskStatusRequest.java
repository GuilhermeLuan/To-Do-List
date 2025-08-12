package dev.guilhermeluan.todo_list.dto;

import dev.guilhermeluan.todo_list.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "O campo 'status' é obrigatório.")
        TaskStatus status
) {

}
