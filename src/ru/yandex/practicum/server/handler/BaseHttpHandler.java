package ru.yandex.practicum.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.common.NetworkHeader;
import ru.yandex.practicum.common.ResponseCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(NetworkHeader.CONTENT_TYPE.name, DEFAULT_CONTENT_TYPE);
        exchange.sendResponseHeaders(ResponseCode.SUCCESS, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendCode(HttpExchange exchange, int code) throws IOException {
        exchange.sendResponseHeaders(code, ZERO_RESPONSE_LENGTH);
        exchange.close();
    }

    protected String[] getPathSegments(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().split(PATH_SEPARATOR);
    }

    protected void sendSuccessCode(HttpExchange exchange) throws IOException {
        sendCode(exchange, ResponseCode.SUCCESS);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendCode(exchange, ResponseCode.NOT_FOUND);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendCode(exchange, ResponseCode.INTERNAL_ERROR);
    }

    protected void sendNotAllowed(HttpExchange exchange) throws IOException {
        sendCode(exchange, ResponseCode.NOT_ALLOWED);
    }

    private static final int ZERO_RESPONSE_LENGTH = 0;
    private static final String DEFAULT_CONTENT_TYPE = "application/json;charset=utf-8";
    private static final String PATH_SEPARATOR = "/";
}
