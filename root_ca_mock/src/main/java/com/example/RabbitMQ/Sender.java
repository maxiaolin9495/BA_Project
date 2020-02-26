package com.example.RabbitMQ;

import com.example.data.CertificateUpdateNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Sender {

    Logger logger = LoggerFactory.getLogger(Sender.class);

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Value(value = "${rabbit.mq.topic-exchange-name}")
    private String TOPIC_EXCHANGE_NAME;

    public void send(CertificateUpdateNotification certificateUpdateNotification, String routingKey) {

        rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_NAME, routingKey, certificateUpdateNotification);
    }
}
