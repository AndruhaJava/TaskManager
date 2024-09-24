package manager;

import history.Status;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.*;

public class TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private int id = 1;

    private int getNextId() {
        return id++;
    }

    public Task addTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Subtask addSubTask(Subtask subtask) {
        subtask.setId(getNextId());
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubTask(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    public Epic addEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Task updateTask(Task task) {
        Integer taskId = task.getId();
        if (!tasks.containsKey(taskId)) {
            return null;
        }
        tasks.replace(taskId, task);
        return task;
    }

    public Subtask updateSubTask(Subtask subtask) {
        Integer subtaskId = subtask.getId();
        if (!subtasks.containsKey(subtaskId)) {
            return null;
        }
        int epicId = subtask.getEpicId();
        Subtask oldSubtask = subtasks.get(subtaskId);
        subtasks.replace(subtaskId, subtask);
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(oldSubtask);
        subtaskList.add(subtask);
        epic.setSubTaskList(subtaskList);
        updateEpicStatus(epic);
        return subtask;
    }

    public Task getTaskFromId(int id) {
        return tasks.get(id);
    }

    public Subtask getSubTaskFromId(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicFromId(int id) {
        return epics.get(id);
    }

    public ArrayList<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Subtask> getListOfSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubTasks();
            epic.setStatus(Status.NEW);
        }
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteTaskFromId(int id) {
        tasks.remove(id);
    }

    public void deleteSubTaskFromId(int id) {
        Subtask subtask = subtasks.get(id);
        int epicId = subtask.getEpicId();
        subtasks.remove(id);
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(subtask);
        epic.setSubTaskList(subtaskList);
        updateEpicStatus(epic);
    }

    public void deleteEpicFromId(int id) {
        ArrayList<Subtask> epicSubtasks = epics.get(id).getSubTaskList();
        epics.remove(id);
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
        }
    }

    private void updateEpicStatus(Epic epic) {
        int doneCount = 0;
        int newCount = 0;
        ArrayList<Subtask> list = epic.getSubTaskList();
        for (Subtask subtask : list) {
            if (subtask.getStatus() == Status.DONE) {
                doneCount++;
            }
            if (subtask.getStatus() == Status.NEW) {
                newCount++;
            }
        }
        if (doneCount == list.size()) {
            epic.setStatus(Status.DONE);
        } else if (newCount == list.size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
