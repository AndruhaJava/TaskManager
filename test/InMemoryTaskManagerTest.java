package test;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    protected Subtask createSubtask(int epicId) {
        return new Subtask("ПОДЗАДАЧА", "ОПИСАНИЕ", epicId);
    }

    protected Epic createEpic() {
        return new Epic("ЭПИК", "ОПИСАНИЕ");
    }

    protected Task createTask() {
        return new Task("ЗАДАЧА", "ОПИСАНИЕ");
    }

    protected InMemoryTaskManager manager;

    @BeforeEach
    void beforeEach() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void shouldBeAddDifferentTaskAndFindById() {
        Task task = createTask();
        Epic epic = createEpic();
        int taskId = manager.addTask(task).getId();
        int epicId = manager.addEpic(epic).getId();
        Subtask subtask = createSubtask(epicId);
        int subtaskId = manager.addSubTask(subtask).getId();
        assertEquals(task, manager.getTaskFromId(taskId), "ERROR");
        assertEquals(epic, manager.getEpicFromId(epicId), "ERROR");
        assertEquals(subtask, manager.getSubTaskFromId(subtaskId), "ERROR");
    }

    @Test
    void shouldBeAddSetIdTaskAndGenerationIdTask() {
        int setId = 1;
        Task task1 = createTask();
        Task task2 = createTask();
        int taskId1 = manager.addTask(task1).getId();
        int taskId2 = manager.addTask(task2).getId();
        task1.setId(setId);
        assertEquals(taskId1, task1.getId(), "ERROR");
    }

    @Override
    protected InMemoryTaskManager createTaskManager() throws IOException {
        manager = new InMemoryTaskManager();
        return manager;
    }
}