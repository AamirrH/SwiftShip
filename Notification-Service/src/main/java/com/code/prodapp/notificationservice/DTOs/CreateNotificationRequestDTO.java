package com.code.prodapp.notificationservice.DTOs;

import com.code.prodapp.notificationservice.entities.NotificationChannel;
import com.code.prodapp.notificationservice.entities.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNotificationRequestDTO {

    @NotNull
    private Long customerId;

    private Long orderNumber;

    @NotNull
    private NotificationType notificationType;

    private NotificationChannel notificationChannel;

    private String recipient;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

}
