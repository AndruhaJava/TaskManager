package http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import manager.Managers;
import manager.TaskManager;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void openConnection() {
        httpServer.start();
    }

    public void closeConnection() {
        httpServer.stop(1);
    }

    public static void main(String[] args) {
        try {
            try {
                new Socket("localhost", PORT).close();
                System.out.println("Port " + PORT + " already in use. Please free the port and retry.");
                return;
            } catch (IOException ignored) {
            }
            TaskManager taskManager = Managers.getDefault();
            HttpTaskServer server = new HttpTaskServer(taskManager);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.closeConnection();
            }));
            server.openConnection();
        } catch (IOException exception) {
            System.out.println("Failed to start server: " + exception.getMessage());
        }
    }
}
