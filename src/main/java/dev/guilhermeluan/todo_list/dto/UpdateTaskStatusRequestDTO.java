package dev.guilhermeluan.todo_list.dto;

import dev.guilhermeluan.todo_list.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequestDTO(
        @NotNull(message = "O campo 'status' é obrigatório.")
        TaskStatus status
) {

}
