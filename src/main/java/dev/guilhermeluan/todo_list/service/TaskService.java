package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.exceptions.ForbiddenException;
import dev.guilhermeluan.todo_list.exceptions.NotFoundException;
import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.model.User;
import dev.guilhermeluan.todo_list.repository.TaskRepository;
import dev.guilhermeluan.todo_list.repository.TaskSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TaskService {
    private final TaskRepository repository;
    private final UserService userService;

    public TaskService(TaskRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public Page<Task> findAll(Long userId, TaskStatus status, Priority priority, LocalDate dueDate, Pageable pageable) {
        Specification<Task> spec = TaskSpecification.buildFilterSpec(userId, status, priority, dueDate);
        return repository.findAll(spec, pageable);
    }

    public Task findByIdOrThrowNotFound(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarefa não encontrada com o id: " + id));
    }

    public Task save(Task task) {
        return repository.save(task);
    }

    public void update(Task taskToUpdate, Long userId) {
        Task taskFound = findByIdOrThrowNotFound(taskToUpdate.getId());
        User user = userService.findUserByIdOrThrowNotFound(userId);

        validateTaskOwnership(taskFound, userId);

        if (taskToUpdate.getStatus() == TaskStatus.DONE && !taskToUpdate.isSubTask()) {
            assertThatAllSubTasksAreCompleted(taskFound);
        }

        taskToUpdate.setUser(user);
        taskToUpdate.setSubTasks(taskFound.getSubTasks());

        repository.save(taskToUpdate);
    }

    public Task createSubTask(Long parentId, Task subTask, Long userId) {
        Task parentTask = findByIdOrThrowNotFound(parentId);

        validateTaskOwnership(parentTask, userId);

        if (parentTask.isSubTask()) {
            throw new BadRequestException("Não é possível aninhar subtarefas. A tarefa pai deve ser uma tarefa principal");
        }

        subTask.setParentTask(parentTask);
        subTask.setIsSubTask(true);
        parentTask.getSubTasks().add(subTask);
        return repository.save(subTask);
    }

    public void delete(Long id, Long userId) {
        Task task = findByIdOrThrowNotFound(id);
        validateTaskOwnership(task, userId);
        repository.deleteById(id);
    }

    public void assertTaskExists(Long id) {
        findByIdOrThrowNotFound(id);
    }

    public Task updateStatus(TaskStatus newStatus, Long id, Long userId) {
        Task existingTask = findByIdOrThrowNotFound(id);
        User user = userService.findUserByIdOrThrowNotFound(userId);

        validateTaskOwnership(existingTask, userId);

        if (newStatus == TaskStatus.DONE && !existingTask.isSubTask()) {
            assertThatAllSubTasksAreCompleted(existingTask);
        }

        existingTask.setUser(user);
        existingTask.setStatus(newStatus);
        return repository.save(existingTask);
    }

    private void assertThatAllSubTasksAreCompleted(Task parentTask) {
        boolean hasIncompleteSubTasks = parentTask.getSubTasks().stream()
                .anyMatch(subTask -> subTask.getStatus() != TaskStatus.DONE);

        if (hasIncompleteSubTasks) {
            throw new BadRequestException("Conclua todas as subtarefas pendentes antes de finalizar a tarefa principal.");
        }
    }

    private void validateTaskOwnership(Task task, Long userId) {
        if (!task.getUser().getId().equals(userId)) {
            throw new ForbiddenException("A tarefa não pertence ao usuário autenticado.");
        }
    }
}
