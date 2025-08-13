package dev.guilhermeluan.todo_list.dto;

public record AuthenticationDTO(
        String login,
        String password
) {}