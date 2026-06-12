package com.tecchieprogramme.notification.service;

import com.tecchieprogramme.notification.dto.NotificationMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationStreamService {

    private static final Long SSE_TIMEOUT_MILLIS = 0L;
    private static final int MAX_RECENT_NOTIFICATIONS = 50;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Deque<NotificationMessage> recentNotifications = new ConcurrentLinkedDeque<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(ex -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Realtime notification stream connected"));
        } catch (IOException ex) {
            emitters.remove(emitter);
            emitter.completeWithError(ex);
        }

        return emitter;
    }

    public List<NotificationMessage> getRecentNotifications() {
        return new ArrayList<>(recentNotifications);
    }

    public void publishOrderPlacedNotification(String orderNumber) {
        NotificationMessage notification = NotificationMessage.builder()
                .id(UUID.randomUUID().toString())
                .type("ORDER_PLACED")
                .title("Order placed")
                .message("Order placed successfully for order number: " + orderNumber)
                .orderNumber(orderNumber)
                .createdAt(Instant.now().toString())
                .build();

        addToRecent(notification);
        broadcast(notification);
    }

    private void addToRecent(NotificationMessage notification) {
        synchronized (recentNotifications) {
            recentNotifications.addFirst(notification);
            while (recentNotifications.size() > MAX_RECENT_NOTIFICATIONS) {
                recentNotifications.removeLast();
            }
        }
    }

    private void broadcast(NotificationMessage notification) {
        List<SseEmitter> closedEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("order-notification")
                        .data(notification));
            } catch (IOException ex) {
                emitter.complete();
                closedEmitters.add(emitter);
            }
        }

        emitters.removeAll(closedEmitters);
    }
}
