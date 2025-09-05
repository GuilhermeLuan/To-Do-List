package dev.guilhermeluan.todo_list.repository.mongo;

import dev.guilhermeluan.todo_list.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public class MongoDBTaskRepositoryImpl implements CustomTaskRepository{
    private final MongoTemplate mongoTemplate;

    public MongoDBTaskRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public Page<Task> findTasksByDynamicFilters(Query query, Pageable pageable) {
        long total = mongoTemplate.count(query, Task.class);
        query.with(pageable);
        List<Task> content = mongoTemplate.find(query, Task.class);

        return PageableExecutionUtils.getPage(content, pageable, () -> total);
    }
}
