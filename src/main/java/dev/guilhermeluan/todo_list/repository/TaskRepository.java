package dev.guilhermeluan.todo_list.repository;

import dev.guilhermeluan.todo_list.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
