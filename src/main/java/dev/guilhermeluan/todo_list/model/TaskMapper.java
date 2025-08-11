package dev.guilhermeluan.todo_list.model;

import dev.guilhermeluan.todo_list.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    Task toTask(TaskPostRequest request);

    Task toTask(UpdateTaskStatusRequest request);

    Task toTask(TaskPutRequest request);

    TaskPostResponse toTaskPostResponse(Task taskSaved);

    @Mapping(target = "parentTaskId", source = "parentTask.id")
    @Mapping(target = "subtasks", source = "subTasks")
    TaskGetResponse toTaskResponseDTO(Task task);

    TaskPutRequest toTaskPutRequest(Task task);
}
