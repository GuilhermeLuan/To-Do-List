package dev.guilhermeluan.todo_list.model;

import dev.guilhermeluan.todo_list.dto.TaskPostRequest;
import dev.guilhermeluan.todo_list.dto.TaskPostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    Task toTask(TaskPostRequest request);

    TaskPostResponse toTaskPostResponse(Task taskSaved);
}
