package com.hireconnect.auth.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hireconnect.auth.event.NotificationEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.notification-queue}")
    private String queue;

    public void sendNotification(NotificationEvent event) {
        logger.info("Publishing notification event for userId={}, type={}",
                event.getRecipientUserId(), event.getType());

        rabbitTemplate.convertAndSend(queue, event);

        logger.info("Notification event published successfully for userId={}", event.getRecipientUserId());
    }
}
