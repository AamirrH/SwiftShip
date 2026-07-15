package com.code.prodapp.notificationservice.repositories;

import com.code.prodapp.notificationservice.entities.Notification;
import com.code.prodapp.notificationservice.entities.NotificationReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByCustomerId(Long customerId);

    List<Notification> findAllByCustomerIdAndReadStatus(Long customerId, NotificationReadStatus readStatus);

    List<Notification> findAllByRecipientIgnoreCase(String recipient);

    List<Notification> findAllByRecipientIgnoreCaseAndReadStatus(String recipient, NotificationReadStatus readStatus);

}
