package com.code.prodapp.orderservice.configs;

import feign.Capability;
import feign.micrometer.MicrometerCapability;
import feign.micrometer.MicrometerObservationCapability;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }









}
