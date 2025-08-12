package dev.guilhermeluan.todo_list.controller;

import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.service.TaskService;
import dev.guilhermeluan.todo_list.utils.FileUtils;
import dev.guilhermeluan.todo_list.utils.TaskUtils;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @Autowired
    private TaskUtils taskUtils;
    @Autowired
    private FileUtils fileUtils;

    @BeforeEach
    public void setUp() {
        tasks = taskUtils.newTaskListWithSubTasks();
    }

    @Test
    @DisplayName("POST /v1/tasks creates a task when is successful")
    void create_CreatesUser_WhenIsSuccessful() throws Exception {
        var taskToSave = taskUtils.newTaskToSave();
        BDDMockito.when(taskService.save(ArgumentMatchers.any())).thenReturn(taskToSave);

        var request = fileUtils.readResourceFile("task/post-request-task-200.json");

        mockMvc.perform(post(URL)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

//    @Test
//    @DisplayName("POST /v1/tasks/99/subtask creates a task when is successful")
//    void createSubTask_CreatesSubTasks_WhenIsSuccessful() throws Exception {
//        var parentTask = taskUtils.newSavedTask();
//        var parentTaskId = parentTask.getId();
//
//        var subTaskToSave = taskUtils.newSubTaskToSave();
//        subTaskToSave.setParentTask(parentTask);
//
//        BDDMockito.when(taskService.save(ArgumentMatchers.any())).thenReturn(parentTask);
//        BDDMockito.when(taskService.save(ArgumentMatchers.any())).thenReturn(subTaskToSave);
//
//        var request = fileUtils.readResourceFile("task/post-request-subtask-200.json");
//
//        mockMvc.perform(post(URL + "/" + parentTaskId + "/subtasks")
//                        .content(request)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isCreated());
//
//    }

    @Test
    @DisplayName("GET  /v1/tasks returns list of all tasks when argument is null")
    void findAll_ReturnsListOfAllAnime_WhenArgumentIsNull() throws Exception {

        Page<Task> tasksPage = new PageImpl<>(
                List.of(taskUtils.newSavedTask()),
                PageRequest.of(0, 10),
                1
        );

        BDDMockito.when(taskService.findAll(ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(Pageable.class))).thenReturn(tasksPage);

        mockMvc.perform(get(URL)
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
                        ArgumentMatchers.eq(TaskStatus.TO_DO),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(Pageable.class)))
                .thenReturn(tasksPage);

        mockMvc.perform(get(URL)
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
}