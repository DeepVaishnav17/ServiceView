package com.techieprogramme.order_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        orderRepository.save(order);
    }
}
