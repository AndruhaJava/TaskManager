package test;

import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    protected Subtask subtask;
    protected Epic epic;
    protected Task task;
    protected TaskManager manager;

    @BeforeEach
    void beforeEach() {
        task = new Task("ЗАДАЧА", "ОПИСАНИЕ");
        epic = new Epic("ЭПИК", "ОПИСАНИЕ");
        manager = Managers.getDefault();
    }

    @Test
    void shouldBeEqualTaskId() {
        int taskId = manager.addTask(task).getId();
        Task savedTask = manager.getTaskFromId(taskId);
        assertEquals(task, savedTask, "ERROR");
    }

    @Test
    void shouldBeEqualSubTaskId() {
        int epicId = manager.addEpic(epic).getId();
        subtask = new Subtask("ПОДЗАДАЧА", "ОПИСАНИЕ", epicId);
        int subtaskId = manager.addSubTask(subtask).getId();
        Subtask savedSubTask = manager.getSubTaskFromId(subtaskId);
        assertEquals(subtask, savedSubTask, "ERROR");
    }

    @Test
    void shouldBeEqualEpicId() {
        int epicId = manager.addEpic(epic).getId();
        Epic savedEpic = manager.getEpicFromId(epicId);
        assertEquals(epic, savedEpic, "ERROR");
    }

    @Test
    void shouldBeNotAddEpicInEpic() {
        int epicId = manager.addEpic(epic).getId();
        subtask = new Subtask("ПОДЗАДАЧА", "ОПИСАНИЕ", epicId);
        int subtaskId1 = manager.addSubTask(subtask).getEpicId();
        assertNotNull(subtaskId1, "ERROR");
    }

    @Test
    void shouldBeNotSubTaskToEpic() {
        int epicId = manager.addEpic(epic).getId();
        subtask = new Subtask("ПОДЗАДАЧА", "ОПИСАНИЕ", epicId);
        int subtaskId1 = manager.addSubTask(subtask).getId();
        subtask = new Subtask("ПОДЗАДАЧАА", "ОПИСАНИЕЕ", epicId);
        int subtaskId2 = manager.addSubTask(subtask).getId();
        assertNotNull(subtaskId2, "ERROR");
    }
}