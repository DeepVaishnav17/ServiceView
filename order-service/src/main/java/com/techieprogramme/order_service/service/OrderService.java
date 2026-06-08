package com.techieprogramme.order_service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
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

        boolean allProductInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);
        if (allProductInStock)
            orderRepository.save(order);
        else
            throw new IllegalArgumentException("Not in stock");
    }
}
