package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.exceptions.NotFoundException;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Task findByIdOrThrowNotFound(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task name already exists"));
    }

    public Task save(Task task) {
        return repository.save(task);
    }

    public void update(Task taskToUpdate) {
        assertTaskExists(taskToUpdate.getId());
        repository.save(taskToUpdate);
    }

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
}
