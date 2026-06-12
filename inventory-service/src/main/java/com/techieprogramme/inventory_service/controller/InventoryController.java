package com.techieprogramme.inventory_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techieprogramme.inventory_service.dto.InventoryResponse;
import com.techieprogramme.inventory_service.dto.InventoryRequest;
import com.techieprogramme.inventory_service.service.InventoryService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateStock(@RequestBody InventoryRequest inventoryRequest) {
        inventoryService.updateStock(inventoryRequest);
    }

    @PostMapping("/decrement")
    @ResponseStatus(HttpStatus.OK)
    public boolean decrementStock(@RequestBody List<InventoryRequest> inventoryRequestList) {
        return inventoryService.decrementStock(inventoryRequestList);
    }
}
