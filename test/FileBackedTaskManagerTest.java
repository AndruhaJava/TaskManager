package test;

import manager.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    public void beforeEach() throws IOException {
        tempFile = File.createTempFile("Temp", ".csv");
        tempFile.deleteOnExit();
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void saveAndLoadEmptyFile() {
        assertTrue(taskManager.getListOfTasks().isEmpty(), "TaskManager not empty");
        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(taskManager.getListOfTasks().isEmpty(), "TaskManager not empty after load");
    }

    @Test
    void loadAndSaveSeveralTasks() {
        Task task1 = new Task("TASK1", "DESCRIPTION1");
        Task task2 = new Task("TASK2", "DESCRIPTION2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        FileBackedTaskManager newTaskManager;
        newTaskManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(2, newTaskManager.getListOfTasks().size(), "SIZE < 2");
        assertEquals(task1.getTitle(), newTaskManager.getTaskFromId(task1.getId()).getTitle(), "TASK1 not equals");
        assertEquals(task2.getTitle(), newTaskManager.getTaskFromId(task2.getId()).getTitle(), "TASK2 not equals");
        assertTrue(tempFile.length() > 0, "File is empty");
    }
}