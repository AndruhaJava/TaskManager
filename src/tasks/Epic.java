package tasks;

import status.Type;

import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {
    private List<Subtask> subtaskList = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
    }

    public Type getType() {
        return Type.EPIC;
    }

    public void addSubTask(Subtask subtask) {
        subtaskList.add(subtask);
    }

    public void deleteSubTask(int id) {
        subtaskList.removeIf(subtask -> subtask.getId() == id);
    }

    public void updateSubTask(Subtask subtask) {
    }

    public void clearSubTasks() {
        subtaskList.clear();
    }

    public List<Subtask> getSubTaskList() {
        return subtaskList;
    }

    public void setSubTaskList(List<Subtask> subtaskList) {
        this.subtaskList = subtaskList;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Tasks.Epic{" +
                "name= " + getTitle() + '\'' +
                ", description = " + getDescription() + '\'' +
                ", id=" + getId() +
                ", subtaskList.size = " + subtaskList.size() +
                ", status = " + getStatus() +
                '}';
    }
}