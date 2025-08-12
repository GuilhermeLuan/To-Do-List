package dev.guilhermeluan.todo_list.model;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = false, nullable = false)
    private String title;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private ZonedDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @Column(nullable = false)
    private boolean isSubTask = false;

    @OneToMany(
            mappedBy = "parentTask",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER // Alterado para EAGER
    )
    private List<Task> subTasks = new ArrayList<>();

    public Task(Long id, String title, String description, ZonedDateTime dueDate, TaskStatus status, Priority priority, Task parentTask, boolean isParent, List<Task> subTasks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.priority = priority;
        this.parentTask = parentTask;
        this.isSubTask = isParent;
        this.subTasks = subTasks;
    }

    public Task() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
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

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Task task)) return false;

        return isSubTask() == task.isSubTask() && Objects.equals(getId(), task.getId()) && Objects.equals(getTitle(), task.getTitle()) && Objects.equals(getDescription(), task.getDescription()) && Objects.equals(getDueDate(), task.getDueDate()) && getStatus() == task.getStatus() && getPriority() == task.getPriority() && Objects.equals(getParentTask(), task.getParentTask()) && Objects.equals(getSubTasks(), task.getSubTasks());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + Objects.hashCode(getTitle());
        result = 31 * result + Objects.hashCode(getDescription());
        result = 31 * result + Objects.hashCode(getDueDate());
        result = 31 * result + Objects.hashCode(getStatus());
        result = 31 * result + Objects.hashCode(getPriority());
        result = 31 * result + Objects.hashCode(getParentTask());
        result = 31 * result + Boolean.hashCode(isSubTask());
        result = 31 * result + Objects.hashCode(getSubTasks());
        return result;
    }
}