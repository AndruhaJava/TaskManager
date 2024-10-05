package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.*;

public interface TaskManager {
    int getNextId();

    Task addTask(Task task);

    Subtask addSubTask(Subtask subtask);

    Epic addEpic(Epic epic);

    Task updateTask(Task task);

    Subtask updateSubTask(Subtask subtask);

    Task getTaskFromId(int id);

    Subtask getSubTaskFromId(int id);

    Epic getEpicFromId(int id);

    ArrayList<Task> getListOfTasks();

    ArrayList<Subtask> getListOfSubTasks();

    ArrayList<Epic> getListOfEpics();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    void deleteTaskFromId(int id);

    void deleteSubTaskFromId(int id);

    void deleteEpicFromId(int id);

    ArrayList<Subtask> getEpicSubtasks(Epic epic);

    ArrayList<Task> getHistory();
}
