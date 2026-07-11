package com.code.prodapp.notificationservice.controllers;

import com.code.prodapp.notificationservice.DTOs.SendEmailRequestDTO;
import com.code.prodapp.notificationservice.DTOs.SendEmailResponseDTO;
import com.code.prodapp.notificationservice.services.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<SendEmailResponseDTO> sendEmail(@Valid @RequestBody SendEmailRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(emailService.sendEmail(requestDTO));
    }
}
