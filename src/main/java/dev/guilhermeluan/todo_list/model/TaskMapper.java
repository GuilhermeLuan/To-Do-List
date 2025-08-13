package dev.guilhermeluan.todo_list.model;

import dev.guilhermeluan.todo_list.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    Task toTask(TaskPostRequestDTO request);

    Task toTask(UpdateTaskStatusRequestDTO request);

    Task toTask(TaskPutRequestDTO request);

    TaskPostResponseDTO toTaskPostResponse(Task taskSaved);

    @Mapping(target = "parentTaskId", source = "parentTask.id")
    @Mapping(target = "subtasks", source = "subTasks")
    TaskGetResponseDTO toTaskResponseDTO(Task task);

    TaskPutRequestDTO toTaskPutRequest(Task task);
}
