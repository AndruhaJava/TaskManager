package test;

import exceptions.ManagerSaveException;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TimeIntersectionTest {
    private TaskManager taskManager;
    private final Task task1 = new Task("task1", "description1");
    private final Task task2 = new Task("task2", "description2");

    @BeforeEach
    public void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void testAddNonOverlappingTask() {
        task1.setStartTime(LocalDateTime.of(2025, 10, 10, 10, 0));
        task1.setDuration(Duration.ofMinutes(30));
        Task addedTask = taskManager.addTask(task1);
        assertEquals(task1.getId(), addedTask.getId(), "NEED SUCCESSFULLY ADDED");
    }

    @Test
    public void testAddOverlappingTask() {
        task1.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 0));
        task1.setDuration(Duration.ofMinutes(30));
        taskManager.addTask(task1);
        task2.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 15));
        task2.setDuration(Duration.ofMinutes(30));
        Exception exception = assertThrows(ManagerSaveException.class, () -> {
            taskManager.addTask(task2);
        });
        assertEquals("Intersected", exception.getMessage());
    }

    @Test
    public void testAddMultipleTasks() {
        task1.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 0));
        task1.setDuration(Duration.ofMinutes(30));
        taskManager.addTask(task1);
        task2.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 30)); // No overlap
        task2.setDuration(Duration.ofMinutes(30));
        taskManager.addTask(task2);
        Task task3 = new Task("task3", "description3");
        task3.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 15)); // Overlaps with task1
        Exception exception = assertThrows(ManagerSaveException.class, () -> {
            taskManager.addTask(task3);
        });
        assertEquals("Intersected", exception.getMessage());
    }

    @Test
    public void testDeleteTaskFreesTimeSlots() {
        task1.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 0));
        task1.setDuration(Duration.ofMinutes(30));
        taskManager.addTask(task1);
        task2.setStartTime(LocalDateTime.of(2023, 10, 10, 10, 15)); // Совпадает с задачей 1
        Exception exception = assertThrows(ManagerSaveException.class, () -> {
            taskManager.addTask(task2);
        });
        assertEquals("Intersected", exception.getMessage());
        taskManager.deleteTaskFromId(task1.getId());
        Task addedTask2 = taskManager.addTask(task2);
        assertEquals(task2.getId(), addedTask2.getId(), "NEED SUCCESSFULLY ADDED");
    }
}
