package com.example.restservice.service;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueService {
    @Bean
    public SimpleMessageConverter converter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("org.springframework.mail.*", "java.util.*"));
        return converter;
    }

    @Bean
    public Queue mailMessagesQueue(){
        return new Queue("mailMessages", true);
    }

    @Bean
    public org.springframework.amqp.core.Queue OCRFileIdsQueue(){
        return new Queue("OCRFileIds", true);
    }
}
