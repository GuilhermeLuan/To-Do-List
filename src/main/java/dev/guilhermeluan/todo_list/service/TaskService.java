package dev.guilhermeluan.todo_list.service;

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
        Specification<Task> spec = Specification.not(null);

        if (status != null) {
            spec = spec.and(TaskSpecification.hasStatus(status));
        }
        if (priority != null) {
            spec = spec.and(TaskSpecification.hasPriority(priority));
        }
        if (dueDate != null) {
            spec = spec.and(TaskSpecification.hasDueDate(dueDate));
        }

        return repository.findAll(spec, pageable);
    }

    public Task findByIdOrThrowNotFound(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
    }

    public Task save(Task task) {
        return repository.save(task);
    }

//    public void update(Task taskToUpdate) {
//
//    }

    public void delete(Long id) {
        assertTaskExists(id);
        repository.deleteById(id);
    }

    public Task createSubTask(Long parentId, Task subTask) {
        Task parentTask = findByIdOrThrowNotFound(parentId);
        subTask.setParentTask(parentTask);
        return repository.save(subTask);
    }

    public void assertTaskExists(Long id) {
        findByIdOrThrowNotFound(id);
    }

    public void assertThatSubTaskDoesNotExist(Long id) {
        Task task = findByIdOrThrowNotFound(id);
        if (!task.getSubTasks().isEmpty()) {
            throw new IllegalStateException("Cannot delete task with existing sub-tasks");
        }
    }

    public Task updateStatus(TaskStatus newStatus, Long id) {
        Task existingTask = findByIdOrThrowNotFound(id);
        existingTask.setStatus(newStatus);
        return repository.save(existingTask);
    }
}
