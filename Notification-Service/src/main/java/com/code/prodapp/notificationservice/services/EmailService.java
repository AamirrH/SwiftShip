package com.code.prodapp.notificationservice.services;

import com.code.prodapp.notificationservice.DTOs.SendEmailRequestDTO;
import com.code.prodapp.notificationservice.DTOs.SendEmailResponseDTO;
import com.code.prodapp.notificationservice.exceptions.EmailDeliveryException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Resend resend;

    @Value("${resend.from-email}")
    private String fromEmail;

    public SendEmailResponseDTO sendEmail(SendEmailRequestDTO requestDTO) {
        log.info("Sending email through Resend to {}", requestDTO.getRecipients());

        CreateEmailOptions emailOptions = buildEmailOptions(requestDTO);

        try {
            CreateEmailResponse response = resend.emails().send(emailOptions);
            return new SendEmailResponseDTO(
                    response.getId(),
                    requestDTO.getRecipients(),
                    requestDTO.getSubject(),
                    "SENT",
                    Instant.now()
            );
        } catch (ResendException exception) {
            throw new EmailDeliveryException("Failed to send email through Resend", exception);
        }
    }

    private CreateEmailOptions buildEmailOptions(SendEmailRequestDTO requestDTO) {
        CreateEmailOptions.Builder builder = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(requestDTO.getRecipients())
                .subject(requestDTO.getSubject());

        if (hasText(requestDTO.getHtmlBody())) {
            builder.html(requestDTO.getHtmlBody());
        }

        if (hasText(requestDTO.getTextBody())) {
            builder.text(requestDTO.getTextBody());
        }

        if (hasItems(requestDTO.getCcRecipients())) {
            builder.cc(requestDTO.getCcRecipients());
        }

        if (hasItems(requestDTO.getBccRecipients())) {
            builder.bcc(requestDTO.getBccRecipients());
        }

        if (hasItems(requestDTO.getReplyTo())) {
            builder.replyTo(requestDTO.getReplyTo());
        }

        return builder.build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasItems(List<String> values) {
        return values != null && !values.isEmpty();
    }
}
