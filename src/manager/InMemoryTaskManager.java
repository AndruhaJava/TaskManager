package manager;

import exceptions.ManagerSaveException;
import history.HistoryManager;
import status.Status;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int id = 1;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    @Override
    public int getNextId() {
        return id++;
    }

    @Override
    public Task addTask(Task task) {
        checkIfIntersectedTaskExist(task);
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Subtask addSubTask(Subtask subtask) {
        subtask.setId(getNextId());
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubTask(subtask);
        checkIfIntersectedTaskExist(subtask);
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) prioritizedTasks.add(subtask);
        updateEpicStatus(epic);
        updateEpicDurationAndTime(epic);
        return subtask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Task updateTask(Task task) {
        checkIfIntersectedTaskExist(task);
        prioritizedTasks.remove(tasks.get(task.getId()));
        Integer taskId = task.getId();
        if (!tasks.containsKey(taskId)) {
            return null;
        }
        tasks.replace(taskId, task);
        if (task.getStartTime() != null) prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Subtask updateSubTask(Subtask subtask) {
        checkIfIntersectedTaskExist(subtask);
        prioritizedTasks.remove(subtasks.get(subtask.getId()));
        Integer subtaskId = subtask.getId();
        if (!subtasks.containsKey(subtaskId)) {
            return null;
        }
        int epicId = subtask.getEpicId();
        Subtask oldSubtask = subtasks.get(subtaskId);
        subtasks.replace(subtaskId, subtask);
        if (subtask.getStartTime() != null) prioritizedTasks.add(subtask);
        Epic epic = epics.get(epicId);
        List<Subtask> subtaskList = epic.getSubTaskList();
        subtaskList.remove(oldSubtask);
        subtaskList.add(subtask);
        epic.setSubTaskList(subtaskList);
        updateEpicStatus(epic);
        updateEpicDurationAndTime(epic);
        return subtask;
    }

    @Override
    public Task getTaskFromId(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubTaskFromId(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicFromId(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public List<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getListOfSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : new ArrayList<>(tasks.keySet())) {
            historyManager.remove(id);
        }
        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        for (Epic epic : epics.values()) {
            updateEpicStatus(epic);
            updateEpicDurationAndTime(epic);
        }
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : new ArrayList<>(epics.keySet())) {
            historyManager.remove(id);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteTaskFromId(int id) {
        prioritizedTasks.remove(tasks.get(id));
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubTaskFromId(int id) {
        Subtask subtask = subtasks.get(id);
        int epicId = subtask.getEpicId();
        prioritizedTasks.remove(subtask);
        subtasks.remove(id);
        Epic epic = epics.get(epicId);
        List<Subtask> subtaskList = epic.getSubTaskList();
        prioritizedTasks.removeAll(subtasks.values());
        subtaskList.remove(subtask);
        epic.setSubTaskList(subtaskList);
        updateEpicStatus(epic);
        updateEpicDurationAndTime(epic);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicFromId(int id) {
        List<Subtask> epicSubtasks = epics.get(id).getSubTaskList();
        epics.remove(id);
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
            historyManager.remove(subtask.getId());
        }
        historyManager.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubTaskList();
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    private boolean isTasksIntersected(Task firstTask, Task secondTask) {
        return (firstTask.getEndTime().isAfter(secondTask.getStartTime()) && firstTask.getEndTime().isBefore(secondTask.getEndTime()) ||
                secondTask.getEndTime().isAfter(firstTask.getStartTime()) && secondTask.getEndTime().isBefore(firstTask.getEndTime()));
    }

    private void checkIfIntersectedTaskExist(Task currentTask) throws ManagerSaveException {
        Optional<Task> intersectedTask = prioritizedTasks.stream().filter(task -> isTasksIntersected(currentTask, task)).findFirst();
        if (intersectedTask.isPresent()) {
            throw new ManagerSaveException("Intersected", new Exception());
        }
    }

    private void updateEpicStatus(Epic epic) {
        int doneCount = 0;
        int newCount = 0;
        List<Subtask> list = epic.getSubTaskList();
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

    public void updateEpicDurationAndTime(Epic epic) {
        List<Subtask> subtasks = epic.getSubTaskList();
        if (subtasks.isEmpty()) {
            epic.setDuration(Duration.ofMinutes(0));
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        Duration duration = Duration.ZERO;
        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() == null || subtask.getEndTime() == null) {
                continue;
            }
            if (startTime == null || subtask.getStartTime().isBefore(startTime)) {
                startTime = subtask.getStartTime();
            }
            if (endTime == null || subtask.getEndTime().isAfter(endTime)) {
                endTime = subtask.getEndTime();
            }
            duration = duration.plus(subtask.getDuration());
        }
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        epic.setDuration(duration);
    }
}
