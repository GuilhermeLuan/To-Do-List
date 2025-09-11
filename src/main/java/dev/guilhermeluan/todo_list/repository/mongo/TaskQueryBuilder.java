package dev.guilhermeluan.todo_list.repository.mongo;

import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskQueryBuilder {
    private final List<Criteria> criteriaList = new ArrayList<>();

    public TaskQueryBuilder withUserId(Long id) {
        if (id != null) {
            criteriaList.add(Criteria.where("userId").is(id));
        }
        return this;
    }

    public TaskQueryBuilder withStatus(TaskStatus status) {
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }
        return this;
    }

    public TaskQueryBuilder withPriority(Priority priority) {
        if (priority != null) {
            criteriaList.add(Criteria.where("priority").is(priority));
        }
        return this;
    }

    public TaskQueryBuilder withDueDate(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            criteriaList.add(Criteria.where("dueDate").is(zonedDateTime));
        }
        return this;
    }

    public Query build() {
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }
}
