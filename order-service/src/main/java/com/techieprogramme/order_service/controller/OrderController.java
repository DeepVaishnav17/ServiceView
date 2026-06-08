package com.techieprogramme.order_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techieprogramme.order_service.dto.OrderRequest;
import com.techieprogramme.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderServicer;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String postMethodName(@RequestBody OrderRequest OrderRequest) {
        return orderServicer.placeOrder(OrderRequest);
        // return "Order Placed Succesful";
    }

}
