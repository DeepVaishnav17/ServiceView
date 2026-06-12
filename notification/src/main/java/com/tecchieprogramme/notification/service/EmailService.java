package com.tecchieprogramme.notification.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendOrderConfirmationEmail(String orderNumber) {
        System.out.println("=========================================");
        System.out.println("📧 MOCK EMAIL NOTIFICATION SENT");
        System.out.println("=========================================");
        System.out.println("To: customer@example.com");
        System.out.println("Subject: Your Marketplace Order Confirmation (#" + orderNumber + ")");
        System.out.println("");
        System.out.println("Hello,");
        System.out.println("");
        System.out.println("Thank you for shopping with us! Your order #" + orderNumber + " has been placed successfully.");
        System.out.println("We will notify you once it ships.");
        System.out.println("");
        System.out.println("Best regards,");
        System.out.println("Marketplace Team");
        System.out.println("=========================================");
    }
}
