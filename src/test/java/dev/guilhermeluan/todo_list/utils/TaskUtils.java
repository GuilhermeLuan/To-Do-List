package dev.guilhermeluan.todo_list.utils;

import dev.guilhermeluan.todo_list.model.Priority;
import dev.guilhermeluan.todo_list.model.Task;
import dev.guilhermeluan.todo_list.model.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TaskUtils {

    public Task newTaskToSave() {
        Task task = new Task();
        task.setTitle("Deploy application to production");
        task.setDescription("Ensure all environment variables are set and the database is migrated.");
        task.setDueDate(ZonedDateTime.now().plusWeeks(1));
        task.setStatus(TaskStatus.TO_DO);
        task.setPriority(Priority.HIGH);
        task.setIsSubTask(false);
        return task;
    }

    public Task newSavedTask() {
        Task task = new Task();
        task.setId(99L);
        task.setTitle("Deploy application to production");
        task.setDescription("Ensure all environment variables are set and the database is migrated.");
        task.setDueDate(ZonedDateTime.now().plusWeeks(1));
        task.setStatus(TaskStatus.TO_DO);
        task.setPriority(Priority.HIGH);
        task.setIsSubTask(false);
        return task;
    }

    public List<Task> newTaskListWithSubTasks() {
        // Tarefa Principal
        Task parentTask = new Task();
        parentTask.setId(1L);
        parentTask.setTitle("Plan company's annual event");
        parentTask.setDescription("Organize the annual confraternization event for all employees.");
        parentTask.setDueDate(ZonedDateTime.now().plusMonths(2));
        parentTask.setStatus(TaskStatus.TO_DO);
        parentTask.setPriority(Priority.HIGH);
        parentTask.setIsSubTask(false);

        // Subtarefa 1
        Task subTask1 = new Task();
        subTask1.setId(2L);
        subTask1.setTitle("Book the venue");
        subTask1.setDescription("Check availability and book the 'Grand Hall' for the event date.");
        subTask1.setDueDate(ZonedDateTime.now().plusDays(10));
        subTask1.setStatus(TaskStatus.TO_DO);
        subTask1.setPriority(Priority.HIGH);
        subTask1.setIsSubTask(true);
        subTask1.setParentTask(parentTask);

        // Subtarefa 2
        Task subTask2 = new Task();
        subTask2.setId(3L);
        subTask2.setTitle("Hire catering service");
        subTask2.setDescription("Get quotes from at least 3 catering companies.");
        subTask2.setDueDate(ZonedDateTime.now().plusDays(15));
        subTask2.setStatus(TaskStatus.TO_DO);
        subTask2.setPriority(Priority.MEDIUM);
        subTask2.setIsSubTask(true);
        subTask2.setParentTask(parentTask);

        // Adiciona as subtarefas na lista da tarefa pai
        parentTask.getSubTasks().add(subTask1);
        parentTask.getSubTasks().add(subTask2);

        return new ArrayList<>(List.of(parentTask, subTask1, subTask2));
    }
}