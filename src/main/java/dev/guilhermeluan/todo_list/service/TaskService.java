package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.exceptions.BadRequestException;
import dev.guilhermeluan.todo_list.exceptions.NotFoundException;
import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
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

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Page<Task> findAll(TaskStatus status, Priority priority, LocalDate dueDate, Pageable pageable) {
        Specification<Task> spec = TaskSpecification.buildFilterSpec(status, priority, dueDate);
        return repository.findAll(spec, pageable);
    }

    public Task findByIdOrThrowNotFound(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tarefa não encontrada com o id: " + id));
    }

    public Task save(Task task) {
        return repository.save(task);
    }

    public void update(Task taskToUpdate){
        Task taskFound = findByIdOrThrowNotFound(taskToUpdate.getId());
        taskToUpdate.setSubTasks(taskFound.getSubTasks());

        repository.save(taskToUpdate);
    }

    public Task createSubTask(Long parentId, Task subTask) {
        Task parentTask = findByIdOrThrowNotFound(parentId);

        if (parentTask.isSubTask()) {
            throw new BadRequestException("Não é possível aninhar subtarefas. A tarefa pai deve ser uma tarefa principal");
        }

        subTask.setParentTask(parentTask);
        subTask.setIsSubTask(true);
        parentTask.getSubTasks().add(subTask);
        return repository.save(subTask);
    }

    public void delete(Long id) {
        assertTaskExists(id);
        repository.deleteById(id);
    }

    public void assertTaskExists(Long id) {
        findByIdOrThrowNotFound(id);
    }

    public Task updateStatus(TaskStatus newStatus, Long id) {
        Task existingTask = findByIdOrThrowNotFound(id);

        if (newStatus == TaskStatus.DONE && !existingTask.isSubTask()) {
            assertThatAllSubTasksAreCompleted(existingTask);
        }

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
}
