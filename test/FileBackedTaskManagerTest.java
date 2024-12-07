package test;

import manager.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
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

    @Override
    protected FileBackedTaskManager createTaskManager() throws IOException {
        tempFile = File.createTempFile("temptasks", ".csv");
        tempFile.deleteOnExit();
        return new FileBackedTaskManager(tempFile);
    }
}