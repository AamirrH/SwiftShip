package com.code.prodapp.orderservice.repository;

import com.code.prodapp.inventoryservice.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Product,Long> {




}
