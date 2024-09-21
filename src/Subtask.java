public class Subtask extends Task {

    int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, Status status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", epicID=" + epicId +
                ", status=" + getStatus() +
                '}';
    }
}