package test;

import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.Status;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
class InMemoryHistoryManagerTest {
    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }

    //убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных
    @Test
    public void shouldReturnOldTaskAfterUpdate() {
        Task buildPc = new Task("Собрать компьютер", "Игровой");
        taskManager.addTask(buildPc);
        taskManager.getTaskFromId(buildPc.getId());
        taskManager.updateTask(new Task(buildPc.getId(), "Купить монитор", "Обычный", Status.IN_PROGRESS));
        ArrayList<Task> tasks = taskManager.getHistory();
        Task oldTask = tasks.getFirst();
        assertEquals(buildPc.getTitle(), oldTask.getTitle(), "В истории не сохранилась предыдущая версия задачи");
        assertEquals(buildPc.getDescription(), oldTask.getDescription(), "В истории не сохранилась предыдущая версия задачи");
    }

    @Test
    void addNewTask() {
        Task task = taskManager.addTask(new Task("Test addNewTask", "Test addNewTask description"));
        Task savedTask = taskManager.getTaskFromId(task.getId());
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        ArrayList<Task> tasks = taskManager.getListOfTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }
}