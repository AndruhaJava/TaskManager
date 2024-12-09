package manager;

import exceptions.ManagerSaveException;
import status.Status;
import status.Type;
import tasks.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) throws ManagerSaveException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            if (file.exists() && Files.size(file.toPath()) > 0) {
                List<String> lines = Files.readAllLines(file.toPath());
                Map<Integer, Task> tempTaskMap = new HashMap<>();
                for (String line : lines.subList(1, lines.size())) {
                    if (!line.isEmpty()) {
                        Task task = manager.fromString(line);
                        tempTaskMap.put(task.getId(), task);
                    }
                }
                for (Task task : tempTaskMap.values()) {
                    if (task.getType().equals(Type.EPIC)) {
                        manager.addEpic((Epic) task);
                    } else if (task.getType().equals(Type.SUBTASK)) {
                        manager.addSubTask((Subtask) task);
                    } else if (task.getType().equals(Type.TASK)) {
                        manager.addTask(task);
                    }
                }
                manager.updateNextTaskId();
            }
            return manager;
        } catch (IOException e) {
            throw new ManagerSaveException("Error loading", e);
        }
    }

    private void save() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id,type,title,status,description,duration,startTime,epic\n");
        for (Epic epic : getListOfEpics()) {
            stringBuilder.append(toStringEpic(epic)).append("\n");
        }
        for (Subtask subtask : getListOfSubTasks()) {
            stringBuilder.append(toStringSub(subtask)).append("\n");
        }
        for (Task task : getListOfTasks()) {
            stringBuilder.append(toStringTask(task)).append("\n");
        }
        try {
            Files.writeString(file.toPath(), stringBuilder.toString(), StandardOpenOption.CREATE);
        } catch (IOException exception) {
            throw new ManagerSaveException("Error saving", exception);
        }
    }

    @Override
    public Task addTask(Task task) {
        Task addedTask = super.addTask(task);
        save();
        return addedTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic addedEpic = super.addEpic(epic);
        save();
        return addedEpic;
    }

    @Override
    public Subtask addSubTask(Subtask subtask) {
        Subtask addedSubTask = super.addSubTask(subtask);
        save();
        return addedSubTask;
    }

    @Override
    public Task updateTask(Task task) {
        super.updateTask(task);
        save();
        return task;
    }

    @Override
    public Subtask updateSubTask(Subtask subtask) {
        super.updateSubTask(subtask);
        save();
        return subtask;
    }

    @Override
    public void deleteTaskFromId(int id) {
        super.deleteTaskFromId(id);
        save();
    }

    private void updateNextTaskId() {
        int maxId = 0;
        for (Task task : getListOfTasks()) {
            maxId = Math.max(maxId, task.getId());
        }
        for (Subtask subtask : getListOfSubTasks()) {
            maxId = Math.max(maxId, subtask.getId());
        }
        for (Epic epic : getListOfEpics()) {
            maxId = Math.max(maxId, epic.getId());
        }
        id = maxId + 1;
    }

    @Override
    public void deleteEpicFromId(int id) {
        super.deleteEpicFromId(id);
        save();
    }

    @Override
    public void deleteSubTaskFromId(int id) {
        super.deleteSubTaskFromId(id);
        save();
    }

    private String toStringTask(Task task) {
        return String.format("%d,%s,%s,%s,%s,", task.getId(), Type.EPIC,
                task.getTitle(), task.getStatus(), task.getDescription(),
                task.getDuration().toMinutes(), task.getStartTime());
    }

    private String toStringSub(Subtask subtask) {
        return String.format("%d,%s,%s,%s,%s,%d", subtask.getId(), Type.SUBTASK,
                subtask.getTitle(), subtask.getStatus(), subtask.getDescription(),
                subtask.getDuration().toMinutes(), subtask.getStartTime(),
                subtask.getEpicId());
    }

    private String toStringEpic(Epic task) {
        return String.format("%d,%s,%s,%s,%s,", task.getId(), Type.EPIC,
                task.getTitle(), task.getStatus(), task.getDescription(),
                task.getDuration().toMinutes(), task.getStartTime());
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        Type type = Type.valueOf(parts[1]);
        String title = parts[2];
        String description = parts[3];
        Status status = Status.valueOf(parts[4]);
        Duration duration = Duration.ofMinutes(Long.parseLong(parts[5]));
        LocalDateTime startTime = null;
        if (parts.length >= 7 && parts[6] != null && !parts[6].isBlank()) {
            startTime = LocalDateTime.parse(parts[6], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        switch (type) {
            case TASK:
                Task task = new Task(title, description);
                task.setId(id);
                task.setStatus(status);
                task.setDuration(duration);
                task.setStartTime(startTime);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                epic.setDuration(duration);
                epic.setStartTime(startTime);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[7]);
                Subtask subtask = new Subtask(title, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}
