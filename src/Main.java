import manager.Managers;
import manager.TaskManager;
import status.Status;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = Managers.getDefault();

        Task buildPc = new Task("Собрать компьютер", "Игровой");
        Task buildPcCreated = inMemoryTaskManager.addTask(buildPc);
        System.out.println(buildPcCreated);

        Task buildPcToUpdate = new Task(buildPc.getId(), "Купить монитор", "Обычный", Status.IN_PROGRESS);
        Task buildPcUpdated = inMemoryTaskManager.updateTask(buildPcToUpdate);
        System.out.println(buildPcUpdated);

        System.out.println(inMemoryTaskManager.getListOfTasks());

        Epic flight = new Epic("Перелет", "Рабочий");
        inMemoryTaskManager.addEpic(flight);
        System.out.println(flight);
        Subtask flightSubtask1 = new Subtask("Собрать чемоданы", "БЫСТРО", flight.getId());
        inMemoryTaskManager.addSubTask(flightSubtask1);
        System.out.println(flight);
        flightSubtask1.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubTask(flightSubtask1);
        System.out.println(flight);

        Epic removal = new Epic("Переезд", "Срочный");
        inMemoryTaskManager.addEpic(removal);
        System.out.println(removal);
        Subtask removalSubtask1 = new Subtask("Собрать вещи", "ВСЕ", removal.getId());
        Subtask removalSubtask2 = new Subtask("Купить мебель", "В ЗАЛ", removal.getId());
        inMemoryTaskManager.addSubTask(removalSubtask1);
        inMemoryTaskManager.addSubTask(removalSubtask2);
        System.out.println(removal);
        removalSubtask2.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubTask(removalSubtask2);
        System.out.println(removal);

        System.out.println(inMemoryTaskManager.getListOfSubTasks());

        System.out.println(inMemoryTaskManager.getListOfEpics());
        inMemoryTaskManager.deleteEpicFromId(flight.getId());
        System.out.println(inMemoryTaskManager.getListOfEpics());
        inMemoryTaskManager.deleteSubTaskFromId(removalSubtask2.getId());
        System.out.println(inMemoryTaskManager.getListOfEpics());

        System.out.println("Задачи:");
        for (Task task : inMemoryTaskManager.getListOfTasks()) {
            System.out.println(task);
        }
        System.out.println("Подзадачи:");
        for (Task subtask : inMemoryTaskManager.getListOfSubTasks()) {
            System.out.println(subtask);
        }
        System.out.println("Эпики:");
        for (Epic epic : inMemoryTaskManager.getListOfEpics()) {
            System.out.println(epic);

            for (Task task : inMemoryTaskManager.getEpicSubtasks(epic)) {
                System.out.println("--> " + task);
            }
        }
    }
}