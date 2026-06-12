package com.techieprogramme.inventory_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techieprogramme.inventory_service.dto.InventoryResponse;
import com.techieprogramme.inventory_service.dto.InventoryRequest;
import com.techieprogramme.inventory_service.model.Inventory;
import com.techieprogramme.inventory_service.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        return inventoryRepository.findBySkuCodeIn(skuCode)
                .stream()
                .map(inventory -> InventoryResponse.builder()
                        .skuCode(inventory.getSkuCode())
                        .inStock(inventory.getQuantity() > 0)
                        .quantity(inventory.getQuantity())
                        .build())
                .toList();
    }

    @Transactional
    public void updateStock(InventoryRequest request) {
        Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setSkuCode(request.getSkuCode());
                    newInv.setQuantity(0);
                    return newInv;
                });
        inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public boolean decrementStock(List<InventoryRequest> requests) {
        // First check if all items have sufficient stock
        for (InventoryRequest request : requests) {
            Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode()).orElse(null);
            if (inventory == null || inventory.getQuantity() < request.getQuantity()) {
                return false; // Not enough stock for at least one item
            }
        }
        
        // Then perform deductions
        for (InventoryRequest request : requests) {
            Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode()).get();
            inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
            inventoryRepository.save(inventory);
        }
        return true;
    }
}
