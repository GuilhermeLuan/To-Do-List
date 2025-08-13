package dev.guilhermeluan.todo_list.service;

import dev.guilhermeluan.todo_list.exceptions.NotFoundException;
import dev.guilhermeluan.todo_list.model.User;
import dev.guilhermeluan.todo_list.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByLogin(username);
    }

    public User findUserByUsernameOrThrowNotFound(String email) {
        User userFound = repository.findByLogin(email);

        if (userFound == null) {
            throw new NotFoundException("User not found");
        }
        return userFound;
    }

    public User findUserByIdOrThrowNotFound(Long id) {
        User userFound = repository.findById(id);

        if (userFound == null) {
            throw new NotFoundException("User not found");
        }
        return userFound;
    }
}