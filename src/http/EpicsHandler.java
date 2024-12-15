package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import status.HttpMethod;
import status.HttpStatus;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static http.BaseHttpHandler.getTaskId;
import static http.BaseHttpHandler.writeResponse;

public class EpicsHandler implements HttpHandler {
    TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            switch (HttpMethod.valueOf(method)) {
                case GET:
                    getEpicOrEpicsOrEpicSubtasks(exchange);
                    break;
                case POST:
                    addOrUpdateEpic(exchange);
                    break;
                case DELETE:
                    deleteEpicOrEpics(exchange);
                    break;
                default:
                    writeResponse(exchange, "Method not found", HttpStatus.METHOD_NOT_ALLOWED.getCode());
            }
            exchange.close();
        } catch (Exception exception) {
            System.out.println("Error occurred: " + exception.getMessage());
            writeResponse(exchange, "{\"error\": \"" + exception.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        }
    }

    public void getEpicOrEpicsOrEpicSubtasks(HttpExchange exchange) throws IOException {
        String[] splitPath = exchange.getRequestURI().getPath().split("/");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        if (splitPath.length == 2) {
            writeResponse(exchange, gson.toJson(taskManager.getListOfEpics()), HttpStatus.OK.getCode());
            return;
        } else if (splitPath.length == 3) {
            Optional<Integer> epicIdOptional = getTaskId(exchange);
            if (epicIdOptional.isPresent()) {
                if (taskManager.getEpicFromId(epicIdOptional.get()) != null) {
                    writeResponse(exchange, gson.toJson(taskManager.getEpicFromId(epicIdOptional.get())), HttpStatus.OK.getCode());
                    return;
                }
            }
        } else if (splitPath.length == 4) {
            if (!splitPath[3].equals("subtasks")) {
                writeResponse(exchange, "Not found", HttpStatus.NOT_ACCEPTABLE.getCode());
            }
            Optional<Integer> epicIdOptional = getTaskId(exchange);
            if (epicIdOptional.isPresent()) {
                if (taskManager.getEpicFromId(epicIdOptional.get()) != null) {
                    writeResponse(exchange, gson.toJson(taskManager.getEpicSubtasks(taskManager.getEpicFromId(epicIdOptional.get()))),
                            HttpStatus.OK.getCode());
                    return;
                }
            }
        }
        writeResponse(exchange, "Not found", HttpStatus.NOT_FOUND.getCode());
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
            writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
            System.out.println(1);
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Epic epicFromJson = gson.fromJson(jsonObject, Epic.class);
        if (splitPath.length == 2) {
            if (epicFromJson.getSubTaskList() != null && epicFromJson.getSubTaskList().size() > 1) {
                List<Subtask> subtasks = epicFromJson.getSubTaskList();
                for (int i = 0; i < subtasks.size(); i++) {
                    Subtask current = subtasks.get(i);
                    if (current.getStartTime() == null || current.getDuration() == null) {
                        continue;
                    }
                    for (int j = i + 1; j < subtasks.size(); j++) {
                        Subtask other = subtasks.get(j);
                        if (other.getStartTime() == null || other.getDuration() == null) {
                            continue;
                        }
                        if (isTasksOverlap(current, other)) {
                            writeResponse(exchange, "Subtasks time overlaps within epic", HttpStatus.NOT_ACCEPTABLE.getCode());
                            return;
                        }
                    }
                }
            }
            Epic newEpic = taskManager.addEpic(epicFromJson);
            if (newEpic == null) {
                writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
            } else {
                writeResponse(exchange, gson.toJson(newEpic), HttpStatus.OK.getCode());
            }
            return;
        } else if (splitPath.length == 3) {
            Optional<Integer> epicIdOptional = getTaskId(exchange);
            if (epicIdOptional.isPresent()) {
                if (taskManager.getEpicFromId(epicIdOptional.get()) != null) {
                    if (epicFromJson != null) {
                        writeResponse(exchange, gson.toJson(epicFromJson), HttpStatus.OK.getCode());
                    } else {
                        writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
                    }
                    return;
                }
            }
        }
        writeResponse(exchange, "Not found", HttpStatus.NOT_FOUND.getCode());
    }

    public void deleteEpicOrEpics(HttpExchange exchange) throws IOException {
        String[] splitPath = exchange.getRequestURI().getPath().split("/");
        if (splitPath.length == 2) {
            taskManager.deleteAllEpics();
            try (OutputStream os = exchange.getResponseBody()) {
                exchange.sendResponseHeaders(HttpStatus.CREATED.getCode(), 0);
            }
            return;
        } else if (splitPath.length == 3) {
            Optional<Integer> epicIdOptional = getTaskId(exchange);
            if (epicIdOptional.isPresent()) {
                if (taskManager.getEpicFromId(epicIdOptional.get()) != null) {
                    taskManager.deleteEpicFromId(epicIdOptional.get());
                    try (OutputStream os = exchange.getResponseBody()) {
                        exchange.sendResponseHeaders(HttpStatus.CREATED.getCode(), 0);
                    }
                    return;
                }
            }
        }
        writeResponse(exchange, "Not found", HttpStatus.NOT_FOUND.getCode());
    }

    private boolean isTasksOverlap(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }
}
