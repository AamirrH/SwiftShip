package com.code.prodapp.orderservice.repository;

import com.code.prodapp.orderservice.entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Orders,Long> {





}
