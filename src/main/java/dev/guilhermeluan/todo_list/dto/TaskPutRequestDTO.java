package dev.guilhermeluan.todo_list.dto;

import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

public record TaskPutRequestDTO(
        @NotBlank(message = "O campo 'title' é obrigatório.")
        @Size(min = 3, max = 255, message = "O título deve ter entre 3 e 255 caracteres.")
        String title,

        @Size(max = 2000, message = "A descrição não pode exceder 2000 caracteres.")
        String description,

        @Future(message = "A data de vencimento 'dueDate' deve ser uma data futura.")
        ZonedDateTime dueDate,

        TaskStatus status,

        Priority priority) {
}
