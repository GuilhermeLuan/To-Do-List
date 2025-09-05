package dev.guilhermeluan.todo_list;

import dev.guilhermeluan.todo_list.repository.jpa.TaskRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication()
@EnableJpaRepositories(basePackages = "dev.guilhermeluan.todo_list.repository.jpa")
@EnableMongoRepositories(basePackages = "dev.guilhermeluan.todo_list.repository.mongo")
public class TodoListApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoListApplication.class, args);
    }

}
