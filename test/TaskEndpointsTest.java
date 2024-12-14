package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.DurationAdapter;
import http.HttpTaskServer;
import http.LocalDateTimeAdapter;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.HttpStatus;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

public class TaskEndpointsTest {
    private HttpTaskServer httpTaskServer;
    private HttpClient client;
    private static final String DEFAULT_TASKS_URI = "http://localhost:8080/tasks";
    private final Task task1 = new Task("task", "description");
    private final TaskManager taskManager = Managers.getDefault();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @BeforeEach
    public void startServer() throws IOException {
        httpTaskServer = new HttpTaskServer(taskManager);
        client = HttpClient.newHttpClient();
        httpTaskServer.openConnection();
    }

    @AfterEach
    public void endServer() {
        httpTaskServer.closeConnection();
    }

    @Test
    public void getEmptyListTasksTest() throws IOException, InterruptedException {
        URI uri = URI.create(DEFAULT_TASKS_URI);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), "Not right response");
    }

    @Test
    public void addTaskAndGetItTest() throws IOException, InterruptedException {
        URI uri1 = URI.create(DEFAULT_TASKS_URI);
        HttpRequest request1 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .header("Content-Type", "application/json")
                .uri(uri1)
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response1.statusCode(), "Not right response");
        URI uri2 = URI.create(DEFAULT_TASKS_URI);
        HttpRequest request2 = HttpRequest.newBuilder()
                .GET()
                .uri(uri2)
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response2.statusCode(), "Not right response");
    }

    @Test
    public void deleteTaskTest() throws IOException, InterruptedException {
        URI uri1 = URI.create(DEFAULT_TASKS_URI);
        HttpRequest request1 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .header("Content-Type", "application/json")
                .uri(uri1)
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response1.statusCode(), "Not right response");
        URI uri2 = URI.create(DEFAULT_TASKS_URI + "/1");
        HttpRequest request2 = HttpRequest.newBuilder()
                .DELETE()
                .header("Content-Type", "application/json")
                .uri(uri2)
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.CREATED.getCode(), response2.statusCode(), "Not right response");
    }
}