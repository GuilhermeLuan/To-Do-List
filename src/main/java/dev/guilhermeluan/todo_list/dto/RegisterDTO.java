package dev.guilhermeluan.todo_list.dto;

import dev.guilhermeluan.todo_list.model.UserRole;

public record RegisterDTO(
    String login,
    String password,
    UserRole role
) {
}