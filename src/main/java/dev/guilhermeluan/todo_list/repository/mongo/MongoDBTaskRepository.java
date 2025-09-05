package dev.guilhermeluan.todo_list.repository.mongo;

import dev.guilhermeluan.todo_list.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDBTaskRepository extends MongoRepository<Task, String>, CustomTaskRepository {
}
