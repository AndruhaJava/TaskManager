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
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

public class HistoryEndpointsTest {
    private HttpTaskServer httpTaskServer;
    private HttpClient client;
    private static final String DEFAULT_HISTORY_URI = "http://localhost:8080/history";
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
    public void getEmptyHistoryListTest() throws IOException, InterruptedException {
        URI uri = URI.create(DEFAULT_HISTORY_URI);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response.statusCode(), "Not right response");
    }

    @Test
    public void getHistoryListTest() throws IOException, InterruptedException {
        URI uri1 = URI.create("http://localhost:8080/epics");
        Epic epic1 = new Epic("epic", "description");
        HttpRequest request1 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1)))
                .header("Content-Type", "application/json")
                .uri(uri1)
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response1.statusCode(), "Not right response");
        URI uri2 = URI.create("http://localhost:8080/subtasks");
        Subtask subtask11 = new Subtask("subtask", "description", 1);
        HttpRequest request2 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask11)))
                .header("Content-Type", "application/json")
                .uri(uri2)
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response2.statusCode(), "Not right response");
        URI uri3 = URI.create("http://localhost:8080/subtasks");
        HttpRequest request3 = HttpRequest.newBuilder()
                .GET()
                .uri(uri3)
                .build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response3.statusCode(), "Not right response");
        URI uri4 = URI.create(DEFAULT_HISTORY_URI);
        HttpRequest request4 = HttpRequest.newBuilder()
                .GET()
                .uri(uri4)
                .build();
        HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(HttpStatus.OK.getCode(), response4.statusCode(), "Not right response");
    }
}