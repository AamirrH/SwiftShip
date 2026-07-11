package com.code.prodapp.notificationservice.DTOs;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SendEmailRequestDTO {

    @NotEmpty
    private List<@Email @NotBlank String> recipients;

    private List<@Email @NotBlank String> ccRecipients;

    private List<@Email @NotBlank String> bccRecipients;

    private List<@Email @NotBlank String> replyTo;

    @NotBlank
    private String subject;

    private String htmlBody;

    private String textBody;

    @AssertTrue(message = "Either htmlBody or textBody is required")
    public boolean isBodyPresent() {
        return hasText(htmlBody) || hasText(textBody);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
