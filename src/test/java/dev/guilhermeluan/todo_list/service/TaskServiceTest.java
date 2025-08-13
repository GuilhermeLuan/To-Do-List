package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.exceptions.NotFoundException;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.repository.TaskRepository;
import dev.guilhermeluan.todo_list.utils.TaskUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @InjectMocks
    private TaskService taskService;
    private TaskUtils taskUtils = new TaskUtils();
    @Mock
    private TaskRepository taskRepository;

    private List<Task> tasks;

    @BeforeEach
    void setUp() {
        tasks = taskUtils.newTaskListWithSubTasks();
    }

    @Test
    @DisplayName("findById returns a task when successful")
    void findById_ReturnsTask_WhenSuccessful() {
        var taskToFound = taskUtils.newSavedTask();
        long taskId = taskToFound.getId();

        BDDMockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskToFound));

        var taskFound = taskService.findByIdOrThrowNotFound(taskId);

        Assertions.assertThat(taskFound).isEqualTo(taskToFound);
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId);
    }

    @Test
    @DisplayName("findById throws NotFoundException when task is not found")
    void findById_throwsNotFoundException_WhenTaskIsNotFound() {
        var taskToDelete = tasks.getFirst();

        BDDMockito.when(taskRepository.findById(taskToDelete.getId()))
                .thenThrow(NotFoundException.class);


        Assertions.assertThatException().isThrownBy(
                () -> taskService.findByIdOrThrowNotFound(taskToDelete.getId())
        ).isInstanceOf(NotFoundException.class);

        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskToDelete.getId());
    }

    @Test
    @DisplayName("save saves a task when successful")
    void save_CreatesTasks_WhenSuccessful() {
        var taskToSave = taskUtils.newTaskToSave();

        BDDMockito.when(taskRepository.save(taskToSave)).thenReturn(taskToSave);

        var taskSaved = taskService.save(taskToSave);

        Assertions.assertThat(taskSaved).isEqualTo(taskToSave);
        Mockito.verify(taskRepository, Mockito.times(1)).save(taskToSave);
    }

    @Test
    @DisplayName("createSubTask creates a subtask when successful")
    void createSubTask_CreatesSubTask_WhenSuccessful() {
        var parentTask = tasks.getFirst();
        var parentTaskId = parentTask.getId();
        var subTaskToCreate = tasks.get(1);

        BDDMockito.when(taskRepository.findById(parentTaskId)).thenReturn(Optional.of(parentTask));
        BDDMockito.when(taskRepository.save(subTaskToCreate)).thenReturn(subTaskToCreate);

        var subTaskCreated = taskService.createSubTask( parentTaskId, subTaskToCreate, 1L);

        Assertions.assertThat(subTaskCreated).isEqualTo(subTaskToCreate);
        Assertions.assertThat(subTaskCreated.getParentTask()).isEqualTo(parentTask);
        Assertions.assertThat(subTaskCreated.isSubTask()).isTrue();
        Assertions.assertThat(parentTask.getSubTasks()).contains(subTaskToCreate);

        Mockito.verify(taskRepository, Mockito.times(1)).findById(parentTaskId);
        Mockito.verify(taskRepository, Mockito.times(1)).save(subTaskToCreate);

    }

    @Test
    @DisplayName("createSubTask throws BadRequestException when parent task is subtask")
    void createSubTask_ThrowsBadRequestException_WhenParentTaskIsSubTask() {
        var parentTask = tasks.getFirst();
        var parentTaskId = parentTask.getId();
        var subTaskToCreate = tasks.get(1);


        parentTask.setIsSubTask(true); // Simulating that the parent task is a subtask

        BDDMockito.when(taskRepository.findById(parentTaskId)).thenReturn(Optional.of(parentTask));

        Assertions.assertThatException().isThrownBy(
                () -> taskService.createSubTask(parentTaskId, subTaskToCreate, 1L)
        ).isInstanceOf(BadRequestException.class);

        Mockito.verify(taskRepository, Mockito.times(1)).findById(parentTaskId);
        Mockito.verify(taskRepository, Mockito.times(0)).save(subTaskToCreate);
    }

    @Test
    @DisplayName("delete a task when successful")
    void delete_DeletesTasks_WhenSuccessful() {
        var taskToDelete = tasks.getFirst();

        BDDMockito.when(taskRepository.findById(taskToDelete.getId()))
                .thenReturn(Optional.of(taskToDelete));

        BDDMockito.doNothing().when(taskRepository).deleteById(taskToDelete.getId());

        Assertions.assertThatNoException().isThrownBy(
                () -> taskService.delete(taskToDelete.getId())
        );

        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskToDelete.getId());
        Mockito.verify(taskRepository, Mockito.times(1)).deleteById(taskToDelete.getId());

    }

    @Test
    @DisplayName("delete throws NotFoundException when task is not found")
    void delete_ThrowsResponseStatusException_WhenTaskIsNotFound() {
        var taskToDelete = tasks.getFirst();

        BDDMockito.when(taskRepository.findById(taskToDelete.getId()))
                .thenThrow(NotFoundException.class);


        Assertions.assertThatException().isThrownBy(
                () -> taskService.delete(taskToDelete.getId())
        ).isInstanceOf(NotFoundException.class);

        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskToDelete.getId());
        Mockito.verify(taskRepository, Mockito.times(0)).deleteById(taskToDelete.getId());

    }

    @Test
    @DisplayName("updateStatus updates task status when successful")
    void updateStatus_UpdatesTaskStatus_WhenSuccessful() {
        var taskToUpdate = taskUtils.newSavedTask();
        taskToUpdate.setStatus(TaskStatus.TO_DO);
        var newStatus = TaskStatus.DONE;
        var taskId = taskToUpdate.getId();


        BDDMockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskToUpdate));
        BDDMockito.when(taskRepository.save(taskToUpdate)).thenReturn(taskToUpdate);

        var taskUpdated = taskService.updateStatus(newStatus, taskId);


        Assertions.assertThat(taskUpdated.getStatus()).isEqualTo(newStatus);
        Mockito.verify(taskRepository, Mockito.times(1)).save(taskToUpdate);
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId);
    }

    @Test
    @DisplayName("updateStatus updates status to done when all subtasks are completed")
    void updateStatus_UpdatesStatusToDone_WhenAllSubTasksAreCompleted() {
        var parentTaskToUpdate = tasks.getFirst();
        parentTaskToUpdate.setStatus(TaskStatus.IN_PROGRESS);
        var parentTaskId = parentTaskToUpdate.getId();
        var subTask1 = tasks.get(1);
        var subTask2 = tasks.get(2);

        var newStatus = TaskStatus.DONE;

        // Subtasks já completadas
        subTask1.setStatus(TaskStatus.DONE);
        subTask2.setStatus(TaskStatus.DONE);


        BDDMockito.when(taskRepository.findById(parentTaskId)).thenReturn(Optional.of(parentTaskToUpdate));
        BDDMockito.when(taskRepository.save(parentTaskToUpdate)).thenReturn(parentTaskToUpdate);

        var parentTaskUpdated = taskService.updateStatus(newStatus, parentTaskId);

        Assertions.assertThat(parentTaskUpdated.getStatus()).isEqualTo(newStatus);
        Mockito.verify(taskRepository, Mockito.times(1)).save(parentTaskToUpdate);
        Mockito.verify(taskRepository, Mockito.times(1)).findById(parentTaskId);
    }

    @Test
    @DisplayName("updateStatus throws NotFoundException when task is not found")
    void updateStatus_ThrowsNotFoundException_WhenTaskIsNotFound() {
        var taskToUpdate = tasks.getFirst();
        var newStatus = TaskStatus.DONE;

        BDDMockito.when(taskRepository.findById(taskToUpdate.getId()))
                .thenThrow(NotFoundException.class);


        Assertions.assertThatException().isThrownBy(
                () -> taskService.updateStatus(newStatus, taskToUpdate.getId())
        ).isInstanceOf(NotFoundException.class);

        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskToUpdate.getId());
        Mockito.verify(taskRepository, Mockito.times(0)).save(taskToUpdate);
    }

    @Test
    @DisplayName("updateStatus throws BadRequestException when parent task has incomplete subtasks")
    void updateStatus_ThrowsBadRequestException_WhenParentTaskHasIncompleteSubTasks() {
        var parentTask = tasks.getFirst();
        parentTask.setIsSubTask(false);

        // Configurando as subtarefas

        var subTask1 = tasks.get(1);
        subTask1.setParentTask(parentTask);
        subTask1.setIsSubTask(true);
        subTask1.setStatus(TaskStatus.IN_PROGRESS);

        parentTask.getSubTasks().clear();
        parentTask.getSubTasks().add(subTask1);

        var newStatus = TaskStatus.DONE;

        BDDMockito.when(taskRepository.findById(parentTask.getId()))
                .thenReturn(Optional.of(parentTask));


        Assertions.assertThatException().isThrownBy(
                () -> taskService.updateStatus(newStatus, parentTask.getId())
        ).isInstanceOf(BadRequestException.class);

        Mockito.verify(taskRepository, Mockito.times(1)).findById(parentTask.getId());
        Mockito.verify(taskRepository, Mockito.times(0)).save(parentTask);
    }

    @Test
    @DisplayName("update updates a task when successful")
    void update_UpdatesTask_WhenSuccessful() {
        var taskToUpdate = taskUtils.newSavedTask();
        taskToUpdate.setTitle("Updated Title");
        taskToUpdate.setDescription("Updated Description");
        var taskId = taskToUpdate.getId();

        var existingTask = taskUtils.newSavedTask();
        existingTask.setId(taskId);

        BDDMockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        BDDMockito.when(taskRepository.save(taskToUpdate)).thenReturn(taskToUpdate);

        Assertions.assertThatNoException().isThrownBy(
                () -> taskService.update(taskToUpdate)
        );

        Assertions.assertThat(taskToUpdate.getSubTasks()).isEqualTo(existingTask.getSubTasks());
        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId);
        Mockito.verify(taskRepository, Mockito.times(1)).save(taskToUpdate);
    }

    @Test
    @DisplayName("update throws NotFoundException when task is not found")
    void update_ThrowsNotFoundException_WhenTaskIsNotFound() {
        var taskToUpdate = taskUtils.newSavedTask();
        var taskId = taskToUpdate.getId();

        BDDMockito.when(taskRepository.findById(taskId))
                .thenThrow(NotFoundException.class);

        Assertions.assertThatException().isThrownBy(
                () -> taskService.update(taskToUpdate)
        ).isInstanceOf(NotFoundException.class);

        Mockito.verify(taskRepository, Mockito.times(1)).findById(taskId);
        Mockito.verify(taskRepository, Mockito.times(0)).save(taskToUpdate);
    }

}