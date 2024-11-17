package test;

import manager.FileBackedTaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import java.io.File;
import java.io.IOException;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager taskManager1;
    private FileBackedTaskManager taskManager2;

    @Test
    void verifySaveAndLoadFunctionsWithEmptyHistoryList() throws IOException {
        tempFile = File.createTempFile("TempTasks", ".csv");
        tempFile.deleteOnExit();
        taskManager1 = new FileBackedTaskManager(tempFile);
        Epic epic1 = new Epic("Epic1", "Description of epic1");
        Epic epic2 = new Epic("Epic2", "Description of epic2");
        Task task1 = new Task("Task1", "Description of task1");
        Task task2 = new Task("Task2", "Description of task2");
        taskManager1.addEpic(epic1);
        taskManager1.addEpic(epic2);
        taskManager1.addTask(task1);
        taskManager1.addTask(task2);
        Subtask subTask1 = new Subtask("SubTask1", "Description of subTask1", epic1.getId());
        Subtask subTask2 = new Subtask("SubTask2", "Description of subTask2", epic2.getId());
        taskManager1.addSubTask(subTask1);
        taskManager1.addSubTask(subTask2);
        taskManager2 = new FileBackedTaskManager(tempFile);
        Assertions.assertEquals(taskManager1.getListOfTasks(), taskManager2.getListOfTasks(), "The tasks do not match");
        Assertions.assertEquals(taskManager1.getListOfEpics(), taskManager2.getListOfEpics(), "The epics do not match");
        Assertions.assertEquals(taskManager1.getListOfSubTasks(), taskManager2.getListOfSubTasks(), "The subtasks do not match");
        Assertions.assertEquals(taskManager1.getTaskFromId(3).getTitle(), taskManager2.getTaskFromId(3).getTitle(), "TaskTitle not equals");
        Assertions.assertEquals(taskManager1.getTaskFromId(3).getDescription(), taskManager2.getTaskFromId(3).getDescription(), "TaskDescription not equals");
        Assertions.assertEquals(taskManager1.getTaskFromId(3).getStatus(), taskManager2.getTaskFromId(3).getStatus(), "TaskStatus not equals");
        Assertions.assertEquals(taskManager1.getSubTaskFromId(5).getEpicId(), taskManager2.getSubTaskFromId(5).getEpicId(), "Epics from subtasks not equals");
        Assertions.assertEquals(taskManager1.getEpicFromId(1).getSubTaskList(), taskManager2.getEpicFromId(1).getSubTaskList(), "Subtasks from epic not equals");
        Assertions.assertEquals(taskManager1.getEpicFromId(2).toString(), taskManager2.getEpicFromId(2).toString(), "Epics not equals");
    }
}