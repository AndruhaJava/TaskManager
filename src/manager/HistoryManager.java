package manager;

import tasks.Task;

import java.util.*;

public interface HistoryManager {
    void add(Task task);

    ArrayList<Task> getHistory();
}
