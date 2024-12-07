package test;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.Status;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    protected Subtask subtask;
    protected Epic epic;
    protected Task task;
    protected TaskManager manager;
    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void addSubTask() {
        Epic epic = taskManager.addEpic(new Epic("epic1", "descriptionepic1"));
        Subtask subtask = new Subtask("subtask1", "descriptionsubtask1", epic.getId());
        taskManager.addSubTask(subtask);
        assertEquals(1, epic.getSubTaskList().size());
        assertEquals(subtask, epic.getSubTaskList().getFirst());
    }

    @Test
    void deleteSubTask() {
        Epic epic = taskManager.addEpic(new Epic("epic1", "description1"));
        Subtask subtask1 = taskManager.addSubTask(new Subtask("subtask1", "description1", epic.getId()));
        Subtask subtask2 = taskManager.addSubTask(new Subtask("subtask2", "description2", epic.getId()));
        epic.deleteSubTask(subtask1.getId());
        assertEquals(1, epic.getSubTaskList().size());
        assertEquals(subtask2, epic.getSubTaskList().getFirst());
    }

    @Test
    void getSubTasks() {
        Epic epic = taskManager.addEpic(new Epic("epic1", "description1"));
        Subtask subtask1 = taskManager.addSubTask(new Subtask("subtask1", "description1", epic.getId()));
        Subtask subtask2 = taskManager.addSubTask(new Subtask("subtask2", "description2", epic.getId()));
        List<Subtask> subtasks = epic.getSubTaskList();
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(subtask1));
        assertTrue(subtasks.contains(subtask2));
    }

    @Test
    void updateSubTask() {
        Epic epic = taskManager.addEpic(new Epic("epic1", "Описание эпика 1"));
        Subtask subtask1 = taskManager.addSubTask(new Subtask("subtask1", "description1", epic.getId()));
        Subtask subtask2 = taskManager.addSubTask(new Subtask("subtask2", "description2", epic.getId()));
        subtask1.setStatus(Status.DONE);
        epic.updateSubTask(subtask1);
        assertEquals(Status.DONE, epic.getSubTaskList().getFirst().getStatus());
    }

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

    @Test
    void testTaskWithDurationAndStartTime() {
        Task task = new Task("test", "description");
        task.setStartTime(LocalDateTime.of(2025, 10, 10, 10, 0));
        task.setDuration(Duration.ofHours(1));
        taskManager.addTask(task);
        assertEquals(Duration.ofHours(1), task.getDuration());
        assertEquals(LocalDateTime.of(2025, 10, 10, 10, 0), task.getStartTime());
        assertEquals(LocalDateTime.of(2025, 10, 10, 11, 0), task.getEndTime());
    }

    @Test
    void testEpicDurationAndStartTime() {
        Epic epic = taskManager.addEpic(new Epic("test", "description"));
        Subtask subtask1 = new Subtask("test1", "description1", epic.getId());
        subtask1.setDuration(Duration.ofMinutes(30));
        subtask1.setStartTime(LocalDateTime.of(2025, 10, 10, 10, 0));
        Subtask subtask2 = new Subtask("test2", "description2", epic.getId());
        subtask2.setDuration(Duration.ofMinutes(45));
        subtask2.setStartTime(LocalDateTime.of(2025, 10, 10, 11, 0));
        taskManager.addSubTask(subtask1);
        taskManager.addSubTask(subtask2);
        assertEquals(Duration.ofMinutes(75), epic.getDuration());
        assertEquals(LocalDateTime.of(2025, 10, 10, 10, 0), epic.getStartTime());
    }
}