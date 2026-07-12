package com.code.prodapp.orderservice.service;

import com.code.prodapp.orderservice.DTOs.GeoapifyGeocodingResponseDTO;
import com.code.prodapp.orderservice.DTOs.GeoapifyGeocodingResultDTO;
import com.code.prodapp.orderservice.DTOs.GeocodingResultDTO;
import com.code.prodapp.orderservice.exceptions.GeocodingFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoapifyGeocodingService {

    private final RestClient.Builder restClientBuilder;

    @Value("${geoapify.api-key}")
    private String geoapifyApiKey;

    @Value("${geoapify.geocoding-url:https://api.geoapify.com/v1/geocode/search}")
    private String geocodingUrl;

    public GeocodingResultDTO geocodeAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new GeocodingFailedException("Address is required for geocoding");
        }

        if (geoapifyApiKey == null || geoapifyApiKey.isBlank()) {
            throw new GeocodingFailedException("Geoapify API key is missing");
        }

        try {
            GeoapifyGeocodingResponseDTO responseDTO = restClientBuilder.build()
                    .get()
                    .uri(geocodingUrl + "?text={address}&format=json&limit=1&apiKey={apiKey}", address, geoapifyApiKey)
                    .retrieve()
                    .body(GeoapifyGeocodingResponseDTO.class);

            if (responseDTO == null || responseDTO.getResults() == null || responseDTO.getResults().isEmpty()) {
                throw new GeocodingFailedException("Could not geocode address " + address);
            }

            GeoapifyGeocodingResultDTO resultDTO = responseDTO.getResults().getFirst();
            return new GeocodingResultDTO(
                    resultDTO.getLat(),
                    resultDTO.getLon(),
                    resultDTO.getFormatted()
            );
        } catch (GeocodingFailedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new GeocodingFailedException("Geoapify geocoding request failed", exception);
        }
    }
}
