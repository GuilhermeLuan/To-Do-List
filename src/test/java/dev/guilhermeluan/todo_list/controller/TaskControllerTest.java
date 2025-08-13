package dev.guilhermeluan.todo_list.controller;

import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.exceptions.NotFoundException;
import dev.guilhermeluan.todo_list.infra.security.TokenService;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.model.User;
import dev.guilhermeluan.todo_list.model.UserRole;
import dev.guilhermeluan.todo_list.repository.UserRepository;
import dev.guilhermeluan.todo_list.service.TaskService;
import dev.guilhermeluan.todo_list.service.UserService;
import dev.guilhermeluan.todo_list.utils.FileUtils;
import dev.guilhermeluan.todo_list.utils.TaskUtils;
import dev.guilhermeluan.todo_list.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@ComponentScan(basePackages = "dev.guilhermeluan")
class TaskControllerTest {
    private static final String URL = "/v1/tasks";
    @Autowired
    private MockMvc mockMvc;
    private List<Task> tasks;
    @MockitoBean
    private TaskService taskService;
    @MockitoBean
    private TokenService tokenService;
    @Autowired
    private TaskUtils taskUtils;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private UserUtils userUtils;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private UserService userService;

    private User testUser;


    @BeforeEach
    void setUp() {
        tasks = taskUtils.newTaskListWithSubTasks();



        testUser = userUtils.newUserTest();

        BDDMockito.when(tokenService.validateToken(ArgumentMatchers.anyString()))
                .thenReturn(testUser.getUsername());

        BDDMockito.when(userRepository.findByLogin(testUser.getUsername()))
                .thenReturn(testUser);

        BDDMockito.when(userService.findUserByUsernameOrThrowNotFound(testUser.getUsername()))
                .thenReturn(testUser);

        BDDMockito.when(userService.findUserByIdOrThrowNotFound(ArgumentMatchers.anyLong()))
                .thenReturn(testUser);
    }

    private RequestPostProcessor bearerToken() {
        return request -> {
            request.addHeader("Authorization", "Bearer faketoken");
            return request;
        };
    }

    @Test
    @DisplayName("POST /v1/tasks creates a task when is successful")
    void create_CreatesUser_WhenIsSuccessful() throws Exception {
        var taskToSave = taskUtils.newTaskToSave();
        BDDMockito.when(taskService.save(ArgumentMatchers.any())).thenReturn(taskToSave);

        var request = fileUtils.readResourceFile("task/post-request-task-200.json");

        mockMvc.perform(post(URL)
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /v1/tasks/1/subtasks creates a subtask when is successful")
    void createSubTask_CreatesSubTasks_WhenIsSuccessful() throws Exception {
        var parentTask = taskUtils.newSavedTask();
        var parentTaskId = parentTask.getId();

        var subTaskToSave = taskUtils.newSubTaskToSave();
        subTaskToSave.setParentTask(parentTask);

        BDDMockito.when(taskService.createSubTask(ArgumentMatchers.eq(parentTaskId), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(subTaskToSave);

        var request = fileUtils.readResourceFile("task/post-request-subtask-200.json");

        mockMvc.perform(post(URL + "/" + parentTaskId + "/subtasks")
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /v1/tasks/99/subtasks throws NotFoundException when parent task does not exist")
    void createSubTask_ThrowsNotFoundException_WhenParentTaskDoesNotExist() throws Exception {
        var nonExistentParentId = 99L;

        BDDMockito.when(taskService.createSubTask(ArgumentMatchers.eq(nonExistentParentId), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new NotFoundException("Tarefa não encontrada com o id: " + nonExistentParentId));

        var request = fileUtils.readResourceFile("task/post-request-subtask-200.json");

        mockMvc.perform(post(URL + "/" + nonExistentParentId + "/subtasks")
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /v1/tasks/1/subtasks throws BadRequestException when parent task is already a subtask")
    void createSubTask_BadRequestExceptionn_WhenParentTaskIsAlreadySubTask() throws Exception {
        var subTaskId = 1L;

        BDDMockito.when(taskService.createSubTask(ArgumentMatchers.eq(subTaskId), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new BadRequestException("Não é possível aninhar subtarefas. A tarefa pai deve ser uma tarefa principal"));

        var request = fileUtils.readResourceFile("task/post-request-subtask-200.json");

        mockMvc.perform(post(URL + "/" + subTaskId + "/subtasks")
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET  /v1/tasks returns list of all tasks when argument is null")
    void findAll_ReturnsListOfAllAnime_WhenArgumentIsNull() throws Exception {

        Page<Task> tasksPage = new PageImpl<>(
                List.of(taskUtils.newSavedTask()),
                PageRequest.of(0, 10),
                1
        );

        BDDMockito.when(taskService.findAll(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(tasksPage);

        mockMvc.perform(get(URL)
                        .with(bearerToken())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "title,asc"))
                .andDo(print())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].description").exists())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("GET /v1/tasks returns filtered and paginated tasks")
    void findAll_ReturnsFilteredPaginatedTasks_WhenFiltersProvided() throws Exception {
        Task task = taskUtils.newSavedTask();
        task.setStatus(TaskStatus.TO_DO);

        Page<Task> tasksPage = new PageImpl<>(
                List.of(task),
                PageRequest.of(0, 5),
                1
        );

        BDDMockito.when(taskService.findAll(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TaskStatus.TO_DO),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(Pageable.class)))
                .thenReturn(tasksPage);

        mockMvc.perform(get(URL)
                        .with(bearerToken())
                        .param("status", "TO_DO")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "dueDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("TO_DO"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].description").exists())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    @DisplayName("PATCH /v1/tasks/1/status updates task status when is successful")
    void updateStatus_UpdatesTaskStatus_WhenIsSuccessful() throws Exception {
        var taskId = 1L;
        var taskToUpdate = taskUtils.newSavedTask();
        taskToUpdate.setStatus(TaskStatus.IN_PROGRESS);

        BDDMockito.when(taskService.updateStatus(ArgumentMatchers.eq(TaskStatus.IN_PROGRESS), ArgumentMatchers.eq(taskId), ArgumentMatchers.anyLong()))
                .thenReturn(taskToUpdate);

        var request = fileUtils.readResourceFile("task/patch-request-status-200.json");

        mockMvc.perform(patch(URL + "/" + taskId + "/status")
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /v1/tasks/99/status throws NotFoundException when task does not exist")
    void updateStatus_ThrowsNotFoundException_WhenTaskDoesNotExist() throws Exception {
        var nonExistentTaskId = 99L;

        BDDMockito.when(taskService.updateStatus(ArgumentMatchers.any(TaskStatus.class), ArgumentMatchers.eq(nonExistentTaskId), ArgumentMatchers.anyLong()))
                .thenThrow(new NotFoundException("Tarefa não encontrada com o id: " + nonExistentTaskId));

        var request = fileUtils.readResourceFile("task/patch-request-status-200.json");

        mockMvc.perform(patch(URL + "/" + nonExistentTaskId + "/status")
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /v1/tasks/1/status throws BadRequestException when parent task has incomplete subtasks")
    void updateStatus_ThrowsBadRequestException_WhenParentTaskHasIncompleteSubTasks() throws Exception {
        var taskId = 1L;

        BDDMockito.when(taskService.updateStatus(ArgumentMatchers.eq(TaskStatus.DONE), ArgumentMatchers.eq(taskId), ArgumentMatchers.anyLong()))
                .thenThrow(new BadRequestException("Conclua todas as subtarefas pendentes antes de finalizar a tarefa principal."));

        var request = fileUtils.readResourceFile("task/patch-request-status-done-200.json");

        mockMvc.perform(patch(URL + "/" + taskId + "/status")
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /v1/tasks/1 updates task when is successful")
    void update_UpdatesTask_WhenIsSuccessful() throws Exception {
        var taskId = 1L;

        BDDMockito.doNothing().when(taskService).update(ArgumentMatchers.any(), ArgumentMatchers.anyLong());

        var request = fileUtils.readResourceFile("task/put-request-task-200.json");

        mockMvc.perform(put(URL + "/" + taskId)
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /v1/tasks/99 throws NotFoundException when task does not exist")
    void update_ThrowsNotFoundException_WhenTaskDoesNotExist() throws Exception {
        var nonExistentTaskId = 99L;

        BDDMockito.doThrow(new NotFoundException("Tarefa não encontrada com o id: " + nonExistentTaskId))
                .when(taskService).update(ArgumentMatchers.any(), ArgumentMatchers.anyLong());

        var request = fileUtils.readResourceFile("task/put-request-task-200.json");

        mockMvc.perform(put(URL + "/" + nonExistentTaskId)
                        .with(bearerToken())
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /v1/tasks/1 deletes task when is successful")
    void delete_DeletesTask_WhenIsSuccessful() throws Exception {
        var taskId = tasks.getFirst().getId();


        BDDMockito.doNothing().when(taskService).delete(taskId, testUser.getId());

        mockMvc.perform(delete(URL + "/" + taskId)
                        .with(bearerToken()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /v1/tasks/99 throws NotFoundException when task does not exist")
    void delete_ThrowsNotFoundException_WhenTaskDoesNotExist() throws Exception {
        var nonExistentTaskId = 99L;

        BDDMockito.doThrow(new NotFoundException("Tarefa não encontrada com o id: " + nonExistentTaskId))
                .when(taskService).delete(nonExistentTaskId, testUser.getId());

        mockMvc.perform(delete(URL + "/" + nonExistentTaskId)
                        .with(bearerToken()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}