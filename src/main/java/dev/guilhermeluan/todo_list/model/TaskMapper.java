package dev.guilhermeluan.todo_list.model;

import dev.guilhermeluan.todo_list.dto.TaskPostRequest;
import dev.guilhermeluan.todo_list.dto.TaskPostResponse;
import dev.guilhermeluan.todo_list.dto.UpdateTaskStatusRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    Task toTask(TaskPostRequest request);

    Task toTask(UpdateTaskStatusRequest request);

    TaskPostResponse toTaskPostResponse(Task taskSaved);

    dev.guilhermeluan.dtos.TaskGetResponse toTaskResponseDTO(Task task);

    UpdateTaskStatusRequest toTaskPutRequest(Task task);
}
