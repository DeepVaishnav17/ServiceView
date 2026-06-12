package com.techieprogramme.order_service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String username;
    private List<OrderLineItemsDto> orderLineItemsDtos;
}
