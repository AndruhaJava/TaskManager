package http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class BaseHttpHandler {

    protected static void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(responseCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
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
}