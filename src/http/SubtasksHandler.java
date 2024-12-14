package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import status.HttpMethod;
import status.HttpStatus;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static http.BaseHttpHandler.getTaskId;
import static http.BaseHttpHandler.writeResponse;

public class SubtasksHandler implements HttpHandler {
        TaskManager taskManager;

        public SubtasksHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                switch (HttpMethod.valueOf(method)) {
                    case GET:
                        getSubtaskOrSubtasks(exchange);
                        break;
                    case POST:
                        addOrUpdateSubtask(exchange);
                        break;
                    case DELETE:
                        deleteSubtaskOrSubtasks(exchange);
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

        private void getSubtaskOrSubtasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            if (splitPath.length == 2) {
                writeResponse(exchange, gson.toJson(taskManager.getListOfSubTasks()), HttpStatus.OK.getCode());
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> subtaskIdOptional = getTaskId(exchange);
                if (subtaskIdOptional.isPresent()) {
                    if (taskManager.getSubTaskFromId(subtaskIdOptional.get()) != null) {
                        writeResponse(exchange, gson.toJson(taskManager.getSubTaskFromId(subtaskIdOptional.get())), HttpStatus.OK.getCode());
                        return;
                    }
                }
            }
            writeResponse(exchange, "Not found", HttpStatus.NOT_FOUND.getCode());
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
                writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
                return;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Subtask subtaskFromJson = gson.fromJson(jsonObject, Subtask.class);
            if (splitPath.length == 2) {
                Subtask newSubtask = taskManager.addSubTask(subtaskFromJson);
                if (newSubtask == null) {
                    writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
                } else {
                    writeResponse(exchange, gson.toJson(newSubtask), HttpStatus.OK.getCode());
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> subtaskIdOptional = getTaskId(exchange);
                if (subtaskIdOptional.isPresent()) {
                    if (subtaskIdOptional.get() != subtaskFromJson.getId()) {
                        writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
                        return;
                    }
                    if (taskManager.getSubTaskFromId(subtaskIdOptional.get()) != null) {
                        Subtask updateSubtask = taskManager.updateSubTask(subtaskFromJson);
                        if (updateSubtask != null) {
                            writeResponse(exchange, gson.toJson(updateSubtask), HttpStatus.OK.getCode());
                        } else {
                            writeResponse(exchange, "Not Acceptable", HttpStatus.NOT_ACCEPTABLE.getCode());
                            return;
                        }
                    }
                }
            }
            writeResponse(exchange, "Not found", HttpStatus.NOT_FOUND.getCode());
        }

        private void deleteSubtaskOrSubtasks(HttpExchange exchange) throws IOException {
            String[] splitPath = exchange.getRequestURI().getPath().split("/");
            if (splitPath.length == 2) {
                taskManager.deleteAllSubtasks();
                try (OutputStream os = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(HttpStatus.CREATED.getCode(), 0);
                }
                return;
            } else if (splitPath.length == 3) {
                Optional<Integer> subtaskIdOptional = getTaskId(exchange);
                if (subtaskIdOptional.isPresent()) {
                    if (taskManager.getSubTaskFromId(subtaskIdOptional.get()) != null) {
                        taskManager.deleteSubTaskFromId(subtaskIdOptional.get());
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
