import java.util.*;

public class Epic extends Task {

    ArrayList<Subtask> subtaskList = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
    }

    void addSubTask(Subtask subtask) {
        subtaskList.add(subtask);
    }

    void clearSubTasks() {
        subtaskList.clear();
    }

    ArrayList<Subtask> getSubTaskList() {
        return subtaskList;
    }

    void setSubTaskList(ArrayList<Subtask> subtaskList) {
        this.subtaskList = subtaskList;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name= " + getTitle() + '\'' +
                ", description = " + getDescription() + '\'' +
                ", id=" + getId() +
                ", subtaskList.size = " + subtaskList.size() +
                ", status = " + getStatus() +
                '}';
    }
}