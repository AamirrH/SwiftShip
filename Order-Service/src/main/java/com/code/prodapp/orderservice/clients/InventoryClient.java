package com.code.prodapp.orderservice.clients;

import com.code.prodapp.orderservice.DTOs.AddStockRequestDTO;
import com.code.prodapp.orderservice.DTOs.ReduceStockRequestDTO;
import com.code.prodapp.orderservice.DTOs.ReturnedItemsDTO;
import com.code.prodapp.orderservice.DTOs.StockCheckDTO;
import com.code.prodapp.orderservice.entities.Item;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name  ="Inventory-Service",path = "/products")
public interface InventoryClient {

    @PutMapping("/reduceStock")
    void reduceStock(@RequestBody List<ReduceStockRequestDTO> items);

    @PutMapping("/addStock")
    void addStock(@RequestBody List<AddStockRequestDTO> item);

    @PostMapping("/checkStock")
    List<ReturnedItemsDTO> InStockAndReturnPrices(@RequestBody List<StockCheckDTO> stockCheckDTO);


}
