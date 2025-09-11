package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.exceptions.ForbiddenException;
import dev.guilhermeluan.todo_list.exceptions.NotFoundException;
import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.model.User;
import dev.guilhermeluan.todo_list.repository.mongo.MongoDBTaskRepository;
import dev.guilhermeluan.todo_list.repository.mongo.TaskQueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class TaskService {
    private final MongoDBTaskRepository repository;
    private final UserService userService;

    public TaskService(MongoDBTaskRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public Page<Task> findAll(Long userId, TaskStatus status, Priority priority, ZonedDateTime dueDate, Pageable pageable) {
        // TO-DO: Implementar buscar das tarefas associadas ao userId

        Query query = new TaskQueryBuilder().withStatus(status).withPriority(priority).withDueDate(dueDate).build();

        return repository.findTasksByDynamicFilters(query, pageable);


    }

    public Task findByIdOrThrowNotFound(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Tarefa não encontrada com o id: " + id));
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

    public Task createSubTask(String parentId, Task subTask, Long userId) {
        Task parentTask = findByIdOrThrowNotFound(parentId);

        validateTaskOwnership(parentTask, userId);

        if (parentTask.isSubTask()) {
            throw new BadRequestException("Não é possível aninhar subtarefas. A tarefa pai deve ser uma tarefa principal");
        }

        subTask.setIsSubTask(true);
        parentTask.getSubTasks().add(subTask);
        repository.save(parentTask);
        return subTask;
    }

    public void delete(String id, Long userId) {
        Task task = findByIdOrThrowNotFound(id);
        validateTaskOwnership(task, userId);
        repository.deleteById(id);
    }

    public void assertTaskExists(String id) {
        findByIdOrThrowNotFound(id);
    }

    public Task updateStatus(TaskStatus newStatus, String id, Long userId) {
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
        boolean hasIncompleteSubTasks = parentTask.getSubTasks().stream().anyMatch(subTask -> subTask.getStatus() != TaskStatus.DONE);

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
