package test;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    Subtask subtask;
    Epic epic;
    Task task;
    InMemoryTaskManager manager;

    @BeforeEach
    void beforeEach() {
        task = new Task("ЗАДАЧА", "ОПИСАНИЕ");
        epic = new Epic("ЭПИК", "ОПИСАНИЕ");
        manager = new InMemoryTaskManager();
    }

    //проверьте, что экземпляры класса Task равны друг другу, если равен их id
    @Test
    void shouldBeEqualTaskId() {
        int taskId = manager.addTask(task).getId();
        Task savedTask = manager.getTaskFromId(taskId);
        assertEquals(task, savedTask, "ERROR");
    }

    //проверьте, что наследники класса Task равны друг другу, если равен их id;
    @Test
    void shouldBeEqualSubTaskId() {
        int epicId = manager.addEpic(epic).getId();
        subtask = new Subtask("ПОДЗАДАЧА", "ОПИСАНИЕ", epicId);
        int subtaskId = manager.addSubTask(subtask).getId();
        Subtask savedSubTask = manager.getSubTaskFromId(subtaskId);
        assertEquals(subtask, savedSubTask, "ERROR");
    }

    //проверьте, что наследники класса Task равны друг другу, если равен их id
    @Test
    void shouldBeEqualEpicId() {
        int epicId = manager.addEpic(epic).getId();
        Epic savedEpic = manager.getEpicFromId(epicId);
        assertEquals(epic, savedEpic, "ERROR");
    }
}

//Не могу понять, почему epic null
/*
проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи
@Test
void shouldBeNotAddEpicInEpic() {
    int epicId = -1;
    subtask = new Subtask("ПОДЗАДАЧА", "ОПИСАНИЕ", epicId);
    int subtaskId1 = manager.addSubTask(subtask).getEpicId();
    assertNotNull(subtaskId1, "ERROR");
}

проверьте, что объект Subtask нельзя сделать своим же эпиком
@Test
void shouldBeNotSubTaskToEpic() {
    int epicId = manager.addEpic(epic).getId();
    subtask = new Subtask("ПОДЗАДАЧА", "ОПИСАНИЕ", epicId);
    int subtaskId1 = manager.addSubTask(subtask).getId();
    subtask = new Subtask("ПОДЗАДАЧАА", "ОПИСАНИЕЕ", subtaskId1);
    int subtaskId2 = manager.addSubTask(subtask).getId();
    assertNotNull(subtaskId2, "ERROR");
}
*/