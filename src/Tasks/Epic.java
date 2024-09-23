package Tasks;

import History.Status;

import java.util.*;

public class Epic extends Task {

    private ArrayList<Subtask> subtaskList = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
    }

    public void addSubTask(Subtask subtask) {
        subtaskList.add(subtask);
    }

    public void clearSubTasks() {
        subtaskList.clear();
    }

    public ArrayList<Subtask> getSubTaskList() {
        return subtaskList;
    }

    public void setSubTaskList(ArrayList<Subtask> subtaskList) {
        this.subtaskList = subtaskList;
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