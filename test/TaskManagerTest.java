package test;

import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager() throws IOException;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = createTaskManager();
        taskManager = createTaskManager();
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubtasks();
        assertNotNull(taskManager, "TASKMANAGER CAN'T BE NULL");
    }

    @Nested
    class TaskTests {

        @Test
        public void testAddTask() {
            Task task = new Task("TASK1", "DESCRIPTION1");
            taskManager.addTask(task);
            assertEquals(1, taskManager.getListOfTasks().size());
        }

        @Test
        public void testGetTaskById() {
            Task task = new Task("TASK1", "DESCRIPTION1");
            taskManager.addTask(task);
            assertEquals(task, taskManager.getTaskFromId(task.getId()));
        }

        @Test
        public void testUpdateTask() {
            Task task = new Task("TASK1", "DESCRIPTION1");
            taskManager.addTask(task);
            task.setTitle("TASK2");
            taskManager.updateTask(task);
            assertEquals("TASK2", taskManager.getTaskFromId(task.getId()).getTitle());
        }

        @Test
        public void testDeleteTask() {
            Task task = new Task("TASK2", "DESCRIPTION1");
            taskManager.addTask(task);
            taskManager.deleteTaskFromId(task.getId());
            assertEquals(0, taskManager.getListOfTasks().size());
        }
    }

    @Nested
    class EpicTests {

        @Test
        public void testAddEpic() {
            Epic epic = new Epic("EPIC1", "DESCRIPTION1");
            taskManager.addEpic(epic);
            assertEquals(1, taskManager.getListOfEpics().size());
        }

        @Test
        public void testGetEpicById() {
            Epic epic = new Epic("EPIC1", "DESCRIPTION1");
            taskManager.addEpic(epic);
            assertEquals(epic, taskManager.getEpicFromId(epic.getId()));
        }

        @Test
        public void testUpdateEpic() {
            Epic epic = new Epic("EPIC1", "DESCRIPTION1");
            taskManager.addEpic(epic);
            epic.setTitle("EPIC2");
            assertEquals("EPIC2", taskManager.getEpicFromId(epic.getId()).getTitle());
        }

        @Test
        public void testDeleteEpic() {
            Epic epic = new Epic("EPIC1", "DESCRIPTION1");
            taskManager.addEpic(epic);
            taskManager.deleteEpicFromId(epic.getId());
            assertEquals(0, taskManager.getListOfEpics().size());
        }
    }

    @Nested
    class SubtaskTests {

        @Test
        public void testAddSubtask() {
            Epic epic = new Epic("EPIC1", "DESCRIPTION1");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("SUBTASK1", "DESCRIPTION1", epic.getId());
            taskManager.addSubTask(subtask);
            assertEquals(1, taskManager.getListOfSubTasks().size());
        }

        @Test
        public void testGetSubtaskById() {
            Epic epic = new Epic("EPIC1", "DESCRIPTION1");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("SUBTASK1", "DESCRIPTION1", epic.getId());
            taskManager.addSubTask(subtask);
            assertEquals(subtask, taskManager.getSubTaskFromId(subtask.getId()));
        }

        @Test
        public void testUpdateSubtask() {
            Epic epic = new Epic("EPIC1", "DESCRIPTION1");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("SUBTASK1", "DESCRIPTION1", epic.getId());
            taskManager.addSubTask(subtask);
            subtask.setTitle("SUBTASK2");
            taskManager.updateSubTask(subtask);
            assertEquals("SUBTASK2", taskManager.getSubTaskFromId(subtask.getId()).getTitle());
        }

        @Test
        public void testDeleteSubtask() {
            Epic epic = new Epic("EPIC1", "DESCRIPTION1");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("SUBTASK1", "DESCRIPTION1", epic.getId());
            taskManager.addSubTask(subtask);
            taskManager.deleteSubTaskFromId(subtask.getId());
            assertEquals(0, taskManager.getListOfSubTasks().size());
        }
    }
}