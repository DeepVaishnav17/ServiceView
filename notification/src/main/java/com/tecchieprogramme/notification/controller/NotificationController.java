package com.tecchieprogramme.notification.controller;

import com.tecchieprogramme.notification.dto.NotificationMessage;
import com.tecchieprogramme.notification.service.NotificationStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationStreamService notificationStreamService;
    private final com.tecchieprogramme.notification.service.EmailService emailService;

    @org.springframework.web.bind.annotation.PostMapping
    public void receiveNotification(@org.springframework.web.bind.annotation.RequestBody com.tecchieprogramme.notification.event.OrderPlacedEvent event) {
        System.out.println("Received Notification for Order via REST - " + event.getOrderNumber());
        notificationStreamService.publishOrderPlacedNotification(event.getOrderNumber());
        emailService.sendOrderConfirmationEmail(event.getOrderNumber());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        return notificationStreamService.subscribe();
    }

    @GetMapping("/recent")
    public List<NotificationMessage> getRecentNotifications() {
        return notificationStreamService.getRecentNotifications();
    }
}
