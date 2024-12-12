package test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HttpTaskServer;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

public class EpicEndpointsTest {
    private HttpTaskServer httpTaskServer;
    private HttpClient client;
    private static final String DEFAULT_EPICS_URI = "http://localhost:8080/epics";
    private final TaskManager taskManager = Managers.getDefault();
    private final Epic epic1 = new Epic("epic", "description");
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new HttpTaskServer.DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new HttpTaskServer.LocalDateTimeAdapter())
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
    public void getEmptyListEpicsTest() throws IOException, InterruptedException {
        URI uri = URI.create(DEFAULT_EPICS_URI);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode(), "Not right response");
    }

    @Test
    public void addEpicAndGetItTest() throws IOException, InterruptedException {
        URI uri1 = URI.create(DEFAULT_EPICS_URI);
        HttpRequest request1 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1)))
                .header("Content-Type", "application/json")
                .uri(uri1)
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response1.statusCode(), "Not right response");
        URI uri2 = URI.create(DEFAULT_EPICS_URI);
        HttpRequest request2 = HttpRequest.newBuilder()
                .GET()
                .uri(uri2)
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response2.statusCode(), "Not right response");
    }

    @Test
    public void deleteEpicTest() throws IOException, InterruptedException {
        URI uri1 = URI.create(DEFAULT_EPICS_URI);
        HttpRequest request1 = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic1)))
                .header("Content-Type", "application/json")
                .uri(uri1)
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response1.statusCode(), "Not right response");
        URI uri2 = URI.create(DEFAULT_EPICS_URI + "/1");
        HttpRequest request2 = HttpRequest.newBuilder()
                .DELETE()
                .header("Content-Type", "application/json")
                .uri(uri2)
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(201, response2.statusCode(), "Not right response");
    }
}