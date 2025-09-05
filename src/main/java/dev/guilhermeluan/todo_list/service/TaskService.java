package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.exceptions.ForbiddenException;
import dev.guilhermeluan.todo_list.exceptions.NotFoundException;
import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import dev.guilhermeluan.todo_list.model.User;
import dev.guilhermeluan.todo_list.repository.mongo.MongoDBTaskRepository;
import dev.guilhermeluan.todo_list.repository.jpa.TaskRepository;
import dev.guilhermeluan.todo_list.repository.jpa.TaskSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TaskService {
    private final TaskRepository repository;
    private final MongoDBTaskRepository mongoDBRepository;
    private final UserService userService;

    public TaskService(TaskRepository repository, MongoDBTaskRepository mongoDBRepository, UserService userService) {
        this.repository = repository;
        this.mongoDBRepository = mongoDBRepository;
        this.userService = userService;
    }

    public Page<Task> findAll(Long userId, TaskStatus status, Priority priority, LocalDate dueDate, Pageable pageable) {
        // TO-DO: Implementar buscar das tarefas associadas ao userId
        //  e aplicar os filtros opcionais (status, priority, dueDate)
        // Atualmente está retornando todas as tarefas sem filtro.

        Specification<Task> spec = TaskSpecification.buildFilterSpec(userId, status, priority, dueDate);
        return mongoDBRepository.findAll(pageable);
        // return repository.findAll(spec, pageable);
    }

    public Task findByIdOrThrowNotFound(String id) {
        return mongoDBRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarefa não encontrada com o id: " + id));
    }

    public Task save(Task task) {
        return mongoDBRepository.save(task);
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

        mongoDBRepository.save(taskToUpdate);
    }

    public Task createSubTask(String parentId, Task subTask, Long userId) {
        Task parentTask = findByIdOrThrowNotFound(parentId);

        validateTaskOwnership(parentTask, userId);

        if (parentTask.isSubTask()) {
            throw new BadRequestException("Não é possível aninhar subtarefas. A tarefa pai deve ser uma tarefa principal");
        }

        subTask.setParentTask(parentTask);
        subTask.setIsSubTask(true);
        parentTask.getSubTasks().add(subTask);
        return mongoDBRepository.save(subTask);
    }

    public void delete(String id, Long userId) {
        Task task = findByIdOrThrowNotFound(id);
        validateTaskOwnership(task, userId);
        mongoDBRepository.deleteById(id);
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
