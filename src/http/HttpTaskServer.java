package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import manager.Managers;
import manager.TaskManager;
import tasks.*;

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

    public static class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
        @Override
        public JsonElement serialize(Duration duration, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(duration.toMinutes());
        }

        @Override
        public Duration deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return Duration.ofMinutes(json.getAsLong());
        }
    }

    public static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(dateTime.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString());
        }
    }

    protected static void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(responseCode, 0);
            os.write(responseString.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected static Optional<Integer> getTaskId(HttpExchange exchange) {
        String[] splitPath = exchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(splitPath[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    class TasksHandler implements HttpHandler {
        TaskManager taskManager;

        public TasksHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                switch (method) {
                    case "GET":
                        getTaskOrTasks(exchange);
                        break;
                    case "POST":
                        addOrUpdateTask(exchange);
                        break;
                    case "DELETE":
                        deleteTaskOrTasks(exchange);
                        break;
                    default:
                        writeResponse(exchange, "Method not found", 405);
                }
                exchange.close();
            } catch (Exception exception) {
                System.out.println("Error occurred: " + exception.getMessage());
                writeResponse(exchange, "{\"error\": \"" + exception.getMessage() + "\"}", 500);
            }
        }

        private void getTaskOrTasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            if (splitPath.length == 2) {
                writeResponse(exchange, gson.toJson(taskManager.getListOfTasks()), 200);
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> taskIdOptional = getTaskId(exchange);
                if (taskIdOptional.isPresent()) {
                    if (taskManager.getTaskFromId(taskIdOptional.get()) != null) {
                        writeResponse(exchange, gson.toJson(taskManager.getTaskFromId(taskIdOptional.get())), 200);
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }

        private void addOrUpdateTask(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseString(body);
            if (!jsonElement.isJsonObject()) {
                writeResponse(exchange, "Not Acceptable", 406);
                return;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Task taskFromJson = gson.fromJson(jsonObject, Task.class);
            if (splitPath.length == 2) {
                Task newTask = taskManager.addTask(taskFromJson);
                if (newTask == null) {
                    writeResponse(exchange, "Not Acceptable", 406);
                } else {
                    writeResponse(exchange, gson.toJson(newTask), 200);
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> taskIdOptional = getTaskId(exchange);
                if (taskIdOptional.isPresent()) {
                    if (taskManager.getTaskFromId(taskIdOptional.get()) != null) {
                        Task updateTask = taskManager.updateTask(taskFromJson);
                        if (updateTask != null) {
                            writeResponse(exchange, gson.toJson(updateTask), 200);
                        } else {
                            writeResponse(exchange, "Not Acceptable", 406);
                            return;
                        }
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }

        private void deleteTaskOrTasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            if (splitPath.length == 2) {
                taskManager.deleteAllTasks();
                try (OutputStream os = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(201, 0);
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> taskIdOptional = getTaskId(exchange);
                if (taskIdOptional.isPresent()) {
                    if (taskManager.getTaskFromId(taskIdOptional.get()) != null) {
                        taskManager.deleteTaskFromId(taskIdOptional.get());
                        try (OutputStream os = exchange.getResponseBody()) {
                            exchange.sendResponseHeaders(201, 0);
                        }
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }
    }

    class SubtasksHandler implements HttpHandler {
        TaskManager taskManager;

        public SubtasksHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                switch (method) {
                    case "GET":
                        getSubtaskOrSubtasks(exchange);
                        break;
                    case "POST":
                        addOrUpdateSubtask(exchange);
                        break;
                    case "DELETE":
                        deleteSubtaskOrSubtasks(exchange);
                        break;
                    default:
                        writeResponse(exchange, "Method not found", 405);
                }
                exchange.close();
            } catch (Exception exception) {
                System.out.println("Error occurred: " + exception.getMessage());
                writeResponse(exchange, "{\"error\": \"" + exception.getMessage() + "\"}", 500);
            }
        }

        private void getSubtaskOrSubtasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            if (splitPath.length == 2) {
                writeResponse(exchange, gson.toJson(taskManager.getListOfSubTasks()), 200);
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> subtaskIdOptional = getTaskId(exchange);
                if (subtaskIdOptional.isPresent()) {
                    if (taskManager.getSubTaskFromId(subtaskIdOptional.get()) != null) {
                        writeResponse(exchange, gson.toJson(taskManager.getSubTaskFromId(subtaskIdOptional.get())), 200);
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }

        private void addOrUpdateSubtask(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseString(body);
            if (!jsonElement.isJsonObject()) {
                writeResponse(exchange, "Not Acceptable", 406);
                return;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Subtask subtaskFromJson = gson.fromJson(jsonObject, Subtask.class);
            if (splitPath.length == 2) {
                Subtask newSubtask = taskManager.addSubTask(subtaskFromJson);
                if (newSubtask == null) {
                    writeResponse(exchange, "Not Acceptable", 406);
                } else {
                    writeResponse(exchange, gson.toJson(newSubtask), 200);
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> subtaskIdOptional = getTaskId(exchange);
                if (subtaskIdOptional.isPresent()) {
                    if (subtaskIdOptional.get() != subtaskFromJson.getId()) {
                        writeResponse(exchange, "Not Acceptable", 406);
                        return;
                    }
                    if (taskManager.getSubTaskFromId(subtaskIdOptional.get()) != null) {
                        Subtask updateSubtask = taskManager.updateSubTask(subtaskFromJson);
                        if (updateSubtask != null) {
                            writeResponse(exchange, gson.toJson(updateSubtask), 200);
                        } else {
                            writeResponse(exchange, "Not Acceptable", 406);
                            return;
                        }
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }

        private void deleteSubtaskOrSubtasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            if (splitPath.length == 2) {
                taskManager.deleteAllSubtasks();
                try (OutputStream os = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(201, 0);
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> subtaskIdOptional = getTaskId(exchange);
                if (subtaskIdOptional.isPresent()) {
                    if (taskManager.getSubTaskFromId(subtaskIdOptional.get()) != null) {
                        taskManager.deleteSubTaskFromId(subtaskIdOptional.get());
                        try (OutputStream os = exchange.getResponseBody()) {
                            exchange.sendResponseHeaders(201, 0);
                        }
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }
    }

    class EpicsHandler implements HttpHandler {
        TaskManager taskManager;

        public EpicsHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                switch (method) {
                    case "GET":
                        getEpicOrEpicsOrEpicSubtasks(exchange);
                        break;
                    case "POST":
                        addOrUpdateEpic(exchange);
                        break;
                    case "DELETE":
                        deleteEpicOrEpics(exchange);
                        break;
                    default:
                        writeResponse(exchange, "Method not found", 405);
                }
                exchange.close();
            } catch (Exception exception) {
                System.out.println("Error occurred: " + exception.getMessage());
                writeResponse(exchange, "{\"error\": \"" + exception.getMessage() + "\"}", 500);
            }
        }

        public void getEpicOrEpicsOrEpicSubtasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            if (splitPath.length == 2) {
                writeResponse(exchange, gson.toJson(taskManager.getListOfEpics()), 200);
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> epicIdOptional = getTaskId(exchange);
                if (epicIdOptional.isPresent()) {
                    if (taskManager.getEpicFromId(epicIdOptional.get()) != null) {
                        writeResponse(exchange, gson.toJson(taskManager.getEpicFromId(epicIdOptional.get())), 200);
                        return;
                    }
                }
            } else if (splitPath.length == 4) {
                if (!splitPath[3].equals("subtasks")) {
                    writeResponse(exchange, "Not found", 406);
                }
                Optional<Integer> epicIdOptional = getTaskId(exchange);
                if (epicIdOptional.isPresent()) {
                    if (taskManager.getEpicFromId(epicIdOptional.get()) != null) {
                        writeResponse(exchange, gson.toJson(taskManager.getEpicSubtasks(taskManager.getEpicFromId(epicIdOptional.get()))), 200);
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }

        public void addOrUpdateEpic(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonElement jsonElement = JsonParser.parseString(body);
            if (!jsonElement.isJsonObject()) {
                writeResponse(exchange, "Not Acceptable", 406);
                System.out.println(1);
                return;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Epic epicFromJson = gson.fromJson(jsonObject, Epic.class);
            if (splitPath.length == 2) {
                Epic newEpic = taskManager.addEpic(epicFromJson);
                if (newEpic == null) {
                    writeResponse(exchange, "Not Acceptable", 406);
                } else {
                    writeResponse(exchange, gson.toJson(newEpic), 200);
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> epicIdOptional = getTaskId(exchange);
                if (epicIdOptional.isPresent()) {
                    if (taskManager.getEpicFromId(epicIdOptional.get()) != null) {
                        if (epicFromJson != null) {
                            writeResponse(exchange, gson.toJson(epicFromJson), 200);
                        } else {
                            writeResponse(exchange, "Not Acceptable", 406);
                        }
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }

        public void deleteEpicOrEpics(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            if (splitPath.length == 2) {
                taskManager.deleteAllEpics();
                try (OutputStream os = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(201, 0);
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> epicIdOptional = getTaskId(exchange);
                if (epicIdOptional.isPresent()) {
                    if (taskManager.getEpicFromId(epicIdOptional.get()) != null) {
                        taskManager.deleteEpicFromId(epicIdOptional.get());
                        try (OutputStream os = exchange.getResponseBody()) {
                            exchange.sendResponseHeaders(201, 0);
                        }
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", 404);
        }
    }

    class PrioritizedHandler implements HttpHandler {
        TaskManager taskManager;

        public PrioritizedHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    Gson gson = new Gson();
                    writeResponse(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
                } else {
                    writeResponse(exchange, "Method not found", 405);
                }
                exchange.close();
            } catch (Exception exception) {
                System.out.println("Error: " + exception.getMessage());
                writeResponse(exchange, "{\"error\": \"" + exception.getMessage() + "\"}", 500);
            }
        }
    }

    class HistoryHandler implements HttpHandler {
        TaskManager taskManager;

        public HistoryHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    Gson gson = new Gson();
                    writeResponse(exchange, gson.toJson(Managers.getDefaultHistory().getHistory()), 200);
                } else {
                    writeResponse(exchange, "Method not found", 405);
                }
                exchange.close();
            } catch (Exception exception) {
                System.out.println("E: " + exception.getMessage());
                writeResponse(exchange, "{\"error\": \"" + exception.getMessage() + "\"}", 500);
            }
        }
    }
}
