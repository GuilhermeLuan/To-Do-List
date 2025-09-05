package dev.guilhermeluan.todo_list.model;

import jakarta.persistence.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Document("tasks")
public class Task {

    @Id
    private String id;

    private String title;

    private String description;

    @Field(targetType = FieldType.DATE_TIME)
    private ZonedDateTime dueDate;

    private TaskStatus status;

    private Priority priority;

    private boolean isSubTask = false;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private List<Task> subTasks = new ArrayList<>();

    public Task(String id, String title, String description, ZonedDateTime dueDate, TaskStatus status, Priority priority, Task parentTask, boolean isSubTask, User user, List<Task> subTasks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.priority = priority;
        this.isSubTask = isSubTask;
        this.user = user;
        this.subTasks = subTasks;
    }

    public Task() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(ZonedDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public List<Task> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<Task> subTasks) {
        this.subTasks = subTasks;
    }

    public boolean isSubTask() {
        return isSubTask;
    }

    public void setIsSubTask(boolean parent) {
        isSubTask = parent;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Task task)) return false;

        return isSubTask() == task.isSubTask() && Objects.equals(getId(), task.getId()) && Objects.equals(getTitle(), task.getTitle()) && Objects.equals(getDescription(), task.getDescription()) && Objects.equals(getDueDate(), task.getDueDate()) && getStatus() == task.getStatus() && getPriority() == task.getPriority() && Objects.equals(getUser(), task.getUser()) && Objects.equals(getSubTasks(), task.getSubTasks());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getTitle());
        result = 31 * result + Objects.hashCode(getDescription());
        result = 31 * result + Objects.hashCode(getDueDate());
        result = 31 * result + Objects.hashCode(getStatus());
        result = 31 * result + Objects.hashCode(getPriority());
        result = 31 * result + Boolean.hashCode(isSubTask());
        result = 31 * result + Objects.hashCode(getUser());
        result = 31 * result + Objects.hashCode(getSubTasks());
        return result;
    }
}