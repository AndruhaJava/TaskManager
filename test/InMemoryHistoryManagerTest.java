package test;

import history.HistoryManager;
import manager.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task;

    @BeforeEach
    public void beforeEach() {
        task = new Task("TASK", "DESCRIPTION");
    }

    @BeforeEach
    public void beforeEachNext() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void getHistory() {
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertTrue(history.contains(task));
    }

    @Test
    void addTask() {
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void removeTask() {
        historyManager.add(task);
        historyManager.remove(task.getId());
        assertEquals(0, historyManager.getHistory().size());
    }
}