package com.code.prodapp.orderservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomerAddressRequestDTO {

    private String label;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private Double lat;
    private Double lng;
    private Boolean defaultAddress;
}
