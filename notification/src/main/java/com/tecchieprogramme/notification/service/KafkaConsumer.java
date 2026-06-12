package com.tecchieprogramme.notification.service;



import com.tecchieprogramme.notification.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final NotificationStreamService notificationStreamService;
    private final EmailService emailService;
    
    // @KafkaListener(topics = "notificationTopic")
    // public void handleNotification(OrderPlacedEvent orderPlacedEvent) {
    //     System.out.println("Received Notification for Order - " + orderPlacedEvent.getOrderNumber());
    //     notificationStreamService.publishOrderPlacedNotification(orderPlacedEvent.getOrderNumber());
    //     emailService.sendOrderConfirmationEmail(orderPlacedEvent.getOrderNumber());
    // }
}
