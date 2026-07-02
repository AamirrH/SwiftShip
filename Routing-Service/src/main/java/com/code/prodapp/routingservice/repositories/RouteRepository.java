package com.code.prodapp.routingservice.repositories;


import com.code.prodapp.routingservice.DTOs.ModelRouteResponse;
import com.code.prodapp.routingservice.DTOs.RouteRequestDTO;
import okhttp3.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {



}
