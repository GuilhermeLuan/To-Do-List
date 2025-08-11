package dev.guilhermeluan.todo_list.repository;

import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TaskSpecification {
    public static Specification<Task> isTopLevelTask() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parentTask"));
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasDueDate(LocalDate dueDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("dueDate"), dueDate.atStartOfDay(), dueDate.atTime(23, 59, 59));
    }

    public static Specification<Task> buildFilterSpec(TaskStatus status, Priority priority, LocalDate dueDate) {
        Specification<Task> spec = isTopLevelTask();

        if (status != null) {
            spec = spec.and(hasStatus(status));
        }
        if (priority != null) {
            spec = spec.and(hasPriority(priority));
        }
        if (dueDate != null) {
            spec = spec.and(hasDueDate(dueDate));
        }

        return spec;
    }
}
