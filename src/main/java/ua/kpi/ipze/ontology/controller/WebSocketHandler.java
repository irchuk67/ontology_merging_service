package ua.kpi.ipze.ontology.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<String>> messageFutures = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        session.sendMessage(new TextMessage(session.getId()));
        log.info("Established connection of id {} with attributes {}", session.getId(), session.getAttributes());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("Connection of id {} with attributes {} is closed with status {}", session.getId(), session.getAttributes(), status);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String clientMessage = message.getPayload();

        System.out.println("Received from client (" + sessionId + "): " + clientMessage);

        // If there is a CompletableFuture waiting for this session's message, complete it
        CompletableFuture<String> future = messageFutures.get(sessionId);
        log.info("Future is {}", future);
        if (future != null) {
            future.complete(clientMessage);  // Unblock the waiting method
        }
    }

    // Method to wait for a client message, blocking until the message is received
    public String waitForClientMessage(String sessionId) throws Exception {
        // Create a CompletableFuture to wait for the message
        CompletableFuture<String> future = new CompletableFuture<>();
        messageFutures.put(sessionId, future);  // Store the future in the map

        // Wait for the message to arrive and return it
        log.info("Wait for client to respond....");
        String message = future.get();
        log.info("Received message from client");// This will block until the future is completed
        messageFutures.remove(sessionId);  // Clean up after receiving the message
        return message;
    }

    public void sendMessageToClient(String sessionId, String message) throws Exception {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        } else {
            System.out.println("Session not found or closed: " + sessionId);
        }
    }

    public void closeConnection(String sessionId) {
        Optional.ofNullable(sessions.get(sessionId))
                .ifPresent(session -> {
                    try {
                        session.close(CloseStatus.NORMAL);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
