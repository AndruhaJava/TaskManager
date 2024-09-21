import java.util.*;

public class TaskManager {

    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();

    int id = 1;

    int getNextId() {
        return id++;
    }

    Task addTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    Subtask addSubTask(Subtask subtask) {
        subtask.setId(getNextId());
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubTask(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    Epic addEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    Task updateTask(Task task) {
        Integer taskId = task.getId();
        if (taskId == null || !tasks.containsKey(taskId)) {
            return null;
        }
        tasks.replace(taskId, task);
        return task;
    }

    Subtask updateSubTask(Subtask subtask) {
        Integer subtaskId = subtask.getId();
        if (subtaskId == null || !subtasks.containsKey(subtaskId)) {
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

    Epic updateEpic(Epic epic) {
        Integer epicId = epic.getId();
        if (epicId == null || !epics.containsKey(epicId)) {
            return null;
        }
        Epic oldEpic = epics.get(epicId);
        ArrayList<Subtask> oldEpicSubTaskList = oldEpic.getSubTaskList();
        if (!oldEpicSubTaskList.isEmpty()) {
            for (Subtask subtask : oldEpicSubTaskList) {
                subtasks.remove(subtask.getId());
            }
        }
        epics.replace(epicId, epic);
        ArrayList<Subtask> newEpicSubtaskList = epic.getSubTaskList();
        if (!newEpicSubtaskList.isEmpty()) {
            for (Subtask subtask : newEpicSubtaskList) {
                subtasks.put(subtask.getId(), subtask);
            }
        }
        updateEpicStatus(epic);
        return epic;
    }

    Task getTaskFromId(int id) {
        return tasks.get(id);
    }

    Subtask getSubTaskFromId(int id) {
        return subtasks.get(id);
    }

    Epic getEpicFromId(int id) {
        return epics.get(id);
    }

    ArrayList<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    ArrayList<Subtask> getListOfSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    ArrayList<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    void deleteAllTasks() {
        tasks.clear();
    }

    void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubTasks();
            epic.setStatus(Status.NEW);
        }
    }

    void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    void deleteTaskFromId(int id) {
        tasks.remove(id);
    }

    void deleteSubTaskFromId(int id) {
        Subtask subtask = subtasks.get(id);
        int epicId = subtask.getEpicId();
        subtasks.remove(id);
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(subtask);
        epic.setSubTaskList(subtaskList);
        updateEpicStatus(epic);
    }

    void deleteEpicFromId(int id) {
        ArrayList<Subtask> epicSubtasks = epics.get(id).getSubTaskList();
        epics.remove(id);
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
        }
    }

    void updateEpicStatus(Epic epic) {
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
