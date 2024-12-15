package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import status.HttpMethod;
import status.HttpStatus;

import java.io.IOException;

import static http.BaseHttpHandler.writeResponse;

public class PrioritizedHandler implements HttpHandler {
        TaskManager taskManager;

        public PrioritizedHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                if (HttpMethod.valueOf(method) == HttpMethod.GET) {
                    Gson gson = new Gson();
                    writeResponse(exchange, gson.toJson(taskManager.getPrioritizedTasks()), HttpStatus.OK.getCode());
                } else {
                    writeResponse(exchange, "Method not found", HttpStatus.METHOD_NOT_ALLOWED.getCode());
                }
                exchange.close();
            } catch (Exception exception) {
                System.out.println("Error: " + exception.getMessage());
                writeResponse(exchange, "{\"error\": \"" + exception.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR.getCode());
            }
        }
    }
