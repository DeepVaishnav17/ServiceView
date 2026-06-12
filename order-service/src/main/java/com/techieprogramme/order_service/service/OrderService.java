package com.techieprogramme.order_service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.techieprogramme.order_service.dto.InventoryResponse;
import com.techieprogramme.order_service.dto.InventoryRequest;
import com.techieprogramme.order_service.dto.OrderLineItemsDto;
import com.techieprogramme.order_service.dto.OrderRequest;
import com.techieprogramme.order_service.model.Order;
import com.techieprogramme.order_service.model.OrderLineItems;
import com.techieprogramme.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import com.techieprogramme.order_service.event.OrderPlacedEvent;
import com.techieprogramme.order_service.dto.OrderResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    public String placeOrder(OrderRequest orderRequest) {
        List<OrderLineItems> orderLineItems = new ArrayList<>();

        for (OrderLineItemsDto dto : orderRequest.getOrderLineItemsDtos()) {

            OrderLineItems item = new OrderLineItems();

            item.setSkuCode(dto.getSkuCode());
            item.setPrice(dto.getPrice());
            item.setQuantity(dto.getQuantity());

            orderLineItems.add(item);
        }

        Order order = new Order();

        order.setOrderNumber(UUID.randomUUID().toString());
        order.setUsername(orderRequest.getUsername());
        order.setOrderItemsList(orderLineItems);

        List<InventoryRequest> inventoryRequests = orderLineItems.stream()
                .map(item -> new InventoryRequest(item.getSkuCode(), item.getQuantity()))
                .toList();

        Boolean isSuccess = webClientBuilder.build().post()
                .uri("http://inventory-service/api/inventory/decrement")
                .bodyValue(inventoryRequests)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (Boolean.TRUE.equals(isSuccess)) {
            orderRepository.save(order);
            // kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
            try {
                webClientBuilder.build().post()
                        .uri("http://notification-service/api/notification")
                        .bodyValue(new OrderPlacedEvent(order.getOrderNumber()))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            } catch (Exception e) {
                System.err.println("Failed to send notification via REST: " + e.getMessage());
            }
            return "Order Placed Successfully";
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
    }

    public String fallbackMethod(OrderRequest orderRequest, Throwable throwable) {
        return "Inventory service is unavailable. Please try again later.";
    }

    public List<OrderResponse> getOrders(String username) {
        return orderRepository.findByUsername(username).stream().map(order -> {
            OrderResponse response = new OrderResponse();
            response.setId(order.getId());
            response.setOrderNumber(order.getOrderNumber());
            response.setUsername(order.getUsername());
            
            List<OrderLineItemsDto> dtos = order.getOrderItemsList().stream().map(item -> {
                OrderLineItemsDto dto = new OrderLineItemsDto();
                dto.setId(item.getId());
                dto.setSkuCode(item.getSkuCode());
                dto.setPrice(item.getPrice());
                dto.setQuantity(item.getQuantity());
                return dto;
            }).toList();
            
            response.setOrderItemsList(dtos);
            return response;
        }).toList();
    }
}
