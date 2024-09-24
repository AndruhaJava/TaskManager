import history.Status;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task buildPc = new Task("Собрать компьютер", "Игровой");
        Task buildPcCreated = taskManager.addTask(buildPc);
        System.out.println(buildPcCreated);

        Task buildPcToUpdate = new Task(buildPc.getId(), "Купить монитор", "Обычный", Status.IN_PROGRESS);
        Task buildPcUpdated = taskManager.updateTask(buildPcToUpdate);
        System.out.println(buildPcUpdated);

        System.out.println(taskManager.getListOfTasks());

        Epic flight = new Epic("Перелет", "Рабочий");
        taskManager.addEpic(flight);
        System.out.println(flight);
        Subtask flightSubtask1 = new Subtask("Собрать чемоданы", "БЫСТРО", flight.getId());
        taskManager.addSubTask(flightSubtask1);
        System.out.println(flight);
        flightSubtask1.setStatus(Status.DONE);
        taskManager.updateSubTask(flightSubtask1);
        System.out.println(flight);

        Epic removal = new Epic("Переезд", "Срочный");
        taskManager.addEpic(removal);
        System.out.println(removal);
        Subtask removalSubtask1 = new Subtask("Собрать вещи", "ВСЕ", removal.getId());
        Subtask removalSubtask2 = new Subtask("Купить мебель", "В ЗАЛ", removal.getId());
        taskManager.addSubTask(removalSubtask1);
        taskManager.addSubTask(removalSubtask2);
        System.out.println(removal);
        removalSubtask2.setStatus(Status.DONE);
        taskManager.updateSubTask(removalSubtask2);
        System.out.println(removal);

        System.out.println(taskManager.getListOfSubTasks());

        System.out.println(taskManager.getListOfEpics());
        taskManager.deleteEpicFromId(flight.getId());
        System.out.println(taskManager.getListOfEpics());
        taskManager.deleteSubTaskFromId(removalSubtask2.getId());
        System.out.println(taskManager.getListOfEpics());

    }
}