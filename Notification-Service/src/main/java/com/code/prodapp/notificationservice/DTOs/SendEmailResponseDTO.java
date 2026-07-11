package com.code.prodapp.notificationservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailResponseDTO {

    private String resendEmailId;
    private List<String> recipients;
    private String subject;
    private String deliveryStatus;
    private Instant sentAt;

}
