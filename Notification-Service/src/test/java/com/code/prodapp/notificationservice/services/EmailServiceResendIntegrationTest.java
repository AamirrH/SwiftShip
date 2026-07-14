package com.code.prodapp.notificationservice.services;

import com.code.prodapp.notificationservice.DTOs.SendEmailRequestDTO;
import com.code.prodapp.notificationservice.DTOs.SendEmailResponseDTO;
import com.resend.Resend;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class EmailServiceResendIntegrationTest {

    @Test
    void sendsEmailThroughRealResendApi() {
        String apiKey = readConfig("resend.api-key", "RESEND_API_KEY");
        String fromEmail = readConfig("resend.from-email", "RESEND_FROM_EMAIL");
        String recipient = readConfig("resend.test-recipient", "RESEND_TEST_RECIPIENT");

        assumeTrue(hasText(apiKey), "Set -Dresend.api-key or RESEND_API_KEY to run this integration test");
        if (!hasText(fromEmail)) {
            fromEmail = "SwiftShip <onboarding@resend.dev>";
        }
        if (!hasText(recipient)) {
            recipient = "aamirr.1704@gmail.com";
        }

        EmailService emailService = new EmailService(new Resend(apiKey));
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);

        SendEmailRequestDTO requestDTO = new SendEmailRequestDTO();
        requestDTO.setRecipients(List.of(recipient));
        requestDTO.setSubject("SwiftShip Resend integration test");
        requestDTO.setTextBody("SwiftShip real Resend integration test email.");
        requestDTO.setHtmlBody("<p>SwiftShip real Resend integration test email.</p>");

        SendEmailResponseDTO responseDTO = emailService.sendEmail(requestDTO);

        assertThat(responseDTO.getResendEmailId()).isNotBlank();
        assertThat(responseDTO.getRecipients()).containsExactly(recipient);
        assertThat(responseDTO.getDeliveryStatus()).isEqualTo("SENT");
    }

    private String readConfig(String systemPropertyName, String environmentVariableName) {
        String systemProperty = System.getProperty(systemPropertyName);
        if (hasText(systemProperty)) {
            return systemProperty;
        }
        return System.getenv(environmentVariableName);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
