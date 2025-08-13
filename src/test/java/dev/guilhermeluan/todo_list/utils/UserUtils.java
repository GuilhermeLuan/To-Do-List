package dev.guilhermeluan.todo_list.utils;

import dev.guilhermeluan.todo_list.model.*;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserUtils {

    public User newUserTest() {
        return new User(1L, "testuser", "password", UserRole.USER);
    }

}