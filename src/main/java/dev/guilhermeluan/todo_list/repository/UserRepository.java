package dev.guilhermeluan.todo_list.repository;

import dev.guilhermeluan.todo_list.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByLogin(String login);
}
