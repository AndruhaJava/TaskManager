package manager;

import exceptions.ManagerSaveException;
import status.Status;
import status.Type;
import tasks.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        loadFromFile();
    }

    protected void loadFromFile() {
        try {
            if (file.exists() && Files.size(file.toPath()) > 0) {
                List<String> lines = Files.readAllLines(file.toPath());
                Map<Integer, Task> tempTaskMap = new HashMap<>();
                for (String line : lines.subList(1, lines.size())) {
                    if (!line.isEmpty()) {
                        Task task = fromString(line);
                        tempTaskMap.put(task.getId(), task);
                    }
                }
                for (Task task : tempTaskMap.values()) {
                    if (task instanceof Epic) {
                        addEpic((Epic) task);
                    } else if (task instanceof Subtask) {
                        addSubTask((Subtask) task);
                    } else if (task != null) {
                        addTask(task);
                    }
                }
                updateNextTaskId();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error loading", e);
        }
    }

    protected void save() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,title,status,description,epic\n");
        for (Epic epic : getListOfEpics()) {
            sb.append(toString(epic)).append("\n");
        }
        for (Subtask subtask : getListOfSubTasks()) {
            sb.append(toString(subtask)).append("\n");
        }
        for (Task task : getListOfTasks()) {
            sb.append(toString(task)).append("\n");
        }
        try {
            Files.writeString(file.toPath(), sb.toString(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving", e);
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

    private String toString(Task task) {
        if (task instanceof Subtask) {
            return String.format("%d,%s,%s,%s,%s,%d", task.getId(), Type.SUBTASK,
                    task.getTitle(), task.getStatus(), task.getDescription(),
                    ((Subtask) task).getEpicId());
        } else if (task instanceof Epic) {
            return String.format("%d,%s,%s,%s,%s,", task.getId(), Type.EPIC,
                    task.getTitle(), task.getStatus(), task.getDescription());
        } else {
            return String.format("%d,%s,%s,%s,%s,", task.getId(), Type.TASK,
                    task.getTitle(), task.getStatus(), task.getDescription());
        }
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        Type type = Type.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        switch (type) {
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }
}
