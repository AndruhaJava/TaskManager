package test;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryManagerTest {
    private TaskManager taskManager;
    private final Task task1 = new Task("task1", "description1");
    private final Task task2 = new Task("task2", "description2");

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void testHistoryManager() {
        taskManager.addTask(task1);
        taskManager.getTaskFromId(task1.getId());
        taskManager.addTask(task2);
        taskManager.getTaskFromId(task2.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(1));
        assertEquals(task1, history.get(0));
    }

    @Test
    void testHistoryManagerWithDuplicates() {
        taskManager.addTask(task1);
        taskManager.getTaskFromId(task1.getId());
        taskManager.getTaskFromId(task1.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.getFirst());
    }

    @Test
    void testHistoryManagerWithRemove() {
        taskManager.addTask(task1);
        taskManager.getTaskFromId(task1.getId());
        taskManager.addTask(task2);
        taskManager.getTaskFromId(task2.getId());
        taskManager.deleteTaskFromId(task1.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.getFirst());
    }

    @Test
    void testHistoryManagerWithRemoveEpic() {
        Epic epic1 = taskManager.addEpic(new Epic("epic1", "descriptionepic1"));
        Subtask subtask1 = taskManager.addSubTask(new Subtask("subtask1", "descriptionsubtask1", epic1.getId()));
        taskManager.getEpicFromId(epic1.getId());
        taskManager.getSubTaskFromId(subtask1.getId());
        taskManager.deleteEpicFromId(epic1.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(0, history.size());
    }

    @Test
    void testHistoryManagerWithRemoveSubtask() {
        Epic epic1 = taskManager.addEpic(new Epic("epic1", "descriptionepic1"));
        Subtask subtask1 = taskManager.addSubTask(new Subtask("subtask1", "descriptionsubtask1", epic1.getId()));
        taskManager.getEpicFromId(epic1.getId());
        taskManager.getSubTaskFromId(subtask1.getId());
        taskManager.deleteSubTaskFromId(subtask1.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(epic1, history.getFirst());
    }
}
