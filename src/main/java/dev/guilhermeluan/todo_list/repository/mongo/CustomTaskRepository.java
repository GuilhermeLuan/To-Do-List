package dev.guilhermeluan.todo_list.repository.mongo;

import dev.guilhermeluan.todo_list.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;

public interface CustomTaskRepository {
    Page<Task> findTasksByDynamicFilters(Query query, Pageable pageable);
}
