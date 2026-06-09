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
import com.techieprogramme.order_service.dto.OrderLineItemsDto;
import com.techieprogramme.order_service.dto.OrderRequest;
import com.techieprogramme.order_service.model.Order;
import com.techieprogramme.order_service.model.OrderLineItems;
import com.techieprogramme.order_service.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import com.techieprogramme.order_service.event.OrderPlacedEvent;

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
        order.setOrderItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        System.out.println("Length = " + inventoryResponses.length);

        if (inventoryResponses == null || inventoryResponses.length == 0) {
            return "Product not found in inventory";
        }
        System.out.println("Inventory Response Length: " + inventoryResponses.length);

        Arrays.stream(inventoryResponses)
                .forEach(i -> System.out.println(i.getSkuCode() + " " + i.isInStock()));

        boolean allProductInStock = Arrays.stream(inventoryResponses)
                .allMatch(InventoryResponse::isInStock);

        if (allProductInStock) {
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
            return "Order Placed Successfully";
        } else
            return "Product is not in stock";

    }

    public String fallbackMethod(OrderRequest orderRequest, Throwable throwable) {
        return "Inventory service is unavailable. Please try again later.";
    }
}
