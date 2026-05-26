package com.code.prodapp.orderservice.repository;

import com.code.prodapp.orderservice.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long> {

    List<Item> findAllByOrdersId(Long ordersId);

}
