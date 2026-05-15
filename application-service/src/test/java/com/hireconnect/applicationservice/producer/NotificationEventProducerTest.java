package com.hireconnect.applicationservice.producer;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.hireconnect.applicationservice.enums.NotificationType;
import com.hireconnect.applicationservice.event.NotificationEvent;

@ExtendWith(MockitoExtension.class)
class NotificationEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationEventProducer producer;

    @Test
    void sendNotification_PublishesEventToExpectedQueue() {
        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(1L)
                .recipientEmail("test@example.com")
                .title("Title")
                .message("Message")
                .type(NotificationType.APPLICATION)
                .sendEmail(true)
                .build();

        producer.sendNotification(event);

        verify(rabbitTemplate).convertAndSend("hireconnect-notifications", event);
    }
}
