package com.tecchieprogramme.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String id;
    private String type;
    private String title;
    private String message;
    private String orderNumber;
    private String createdAt;
}
