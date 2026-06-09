package com.tecchieprogramme.notification.service;



import com.tecchieprogramme.notification.event.OrderPlacedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @KafkaListener(topics = "notificationTopic")
    public void handleNotification(OrderPlacedEvent orderPlacedEvent) {
        System.out.println("Received Notification for Order - " + orderPlacedEvent.getOrderNumber());
    }
}
