package dev.guilhermeluan.todo_list.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.TaskStatus;

import java.time.ZonedDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskGetResponseDTO(
        Long id,
        String title,
        String description,
        ZonedDateTime dueDate,
        TaskStatus status,
        Priority priority,
        Long parentTaskId,
        List<SubtaskInfo> subtasks
) {

    public record SubtaskInfo(
            Long id,
            String title,
            String description,
            ZonedDateTime dueDate,
            TaskStatus status,
            Priority priority
    ) {
    }
}