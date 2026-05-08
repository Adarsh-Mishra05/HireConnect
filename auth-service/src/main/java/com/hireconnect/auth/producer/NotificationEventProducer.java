package com.hireconnect.auth.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.hireconnect.auth.event.NotificationEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventProducer.class);

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    private final String topic = "hireconnect-notifications";

    public void sendNotification(NotificationEvent event) {
        logger.info("Publishing notification event for userId={}, type={}",
                event.getRecipientUserId(), event.getType());

        kafkaTemplate.send(topic, event);

        logger.info("Notification event published successfully for userId={}", event.getRecipientUserId());
    }
}