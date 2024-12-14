package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import status.HttpMethod;
import status.HttpStatus;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static http.BaseHttpHandler.getTaskId;
import static http.BaseHttpHandler.writeResponse;

public class TasksHandler implements HttpHandler {
        TaskManager taskManager;

        public TasksHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                switch (HttpMethod.valueOf(method)) {
                    case GET:
                        getTaskOrTasks(exchange);
                        break;
                    case POST:
                        addOrUpdateTask(exchange);
                        break;
                    case DELETE:
                        deleteTaskOrTasks(exchange);
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

        private void getTaskOrTasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            if (splitPath.length == 2) {
                writeResponse(exchange, gson.toJson(taskManager.getListOfTasks()), HttpStatus.OK.getCode());
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> taskIdOptional = getTaskId(exchange);
                if (taskIdOptional.isPresent()) {
                    if (taskManager.getTaskFromId(taskIdOptional.get()) != null) {
                        writeResponse(exchange, gson.toJson(taskManager.getTaskFromId(taskIdOptional.get())), HttpStatus.OK.getCode());
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", HttpStatus.NOT_FOUND.getCode());
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
                writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
                return;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Task taskFromJson = gson.fromJson(jsonObject, Task.class);
            if (splitPath.length == 2) {
                Task newTask = taskManager.addTask(taskFromJson);
                if (newTask == null) {
                    writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
                } else {
                    writeResponse(exchange, gson.toJson(newTask), HttpStatus.OK.getCode());
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> taskIdOptional = getTaskId(exchange);
                if (taskIdOptional.isPresent()) {
                    if (taskManager.getTaskFromId(taskIdOptional.get()) != null) {
                        Task updateTask = taskManager.updateTask(taskFromJson);
                        if (updateTask != null) {
                            writeResponse(exchange, gson.toJson(updateTask), HttpStatus.OK.getCode());
                        } else {
                            writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
                            return;
                        }
                    }
                }
            }
            writeResponse(exchange, "Not found", HttpStatus.NOT_FOUND.getCode());
        }

        private void deleteTaskOrTasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            if (splitPath.length == 2) {
                taskManager.deleteAllTasks();
                try (OutputStream os = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(HttpStatus.CREATED.getCode(), 0);
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> taskIdOptional = getTaskId(exchange);
                if (taskIdOptional.isPresent()) {
                    if (taskManager.getTaskFromId(taskIdOptional.get()) != null) {
                        taskManager.deleteTaskFromId(taskIdOptional.get());
                        try (OutputStream os = exchange.getResponseBody()) {
                            exchange.sendResponseHeaders(HttpStatus.CREATED.getCode(), 0);
                        }
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", HttpStatus.NOT_FOUND.getCode());
        }
    }
