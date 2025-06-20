package com.example.restservice.service;

import com.example.restservice.model.User;
import com.example.restservice.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MailService {
    private final JavaMailSenderImpl mailSender;

    private final RabbitTemplate rabbitTemplate;

    private final UserRepository userRepository;

    @Value("${mail.from}")
    private String from;

    public MailService(JavaMailSenderImpl mailSender, RabbitTemplate rabbitTemplate, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.rabbitTemplate = rabbitTemplate;
        this.userRepository = userRepository;
    }

//    @RabbitListener(queues = "mailMessages")
//    private void receiveMailMessage(SimpleMailMessage message) {
//        mailSender.send(message);
//    }

    private void sendMail(String to, String subject, String body) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(from);
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//        rabbitTemplate.convertAndSend("mailMessages", message);
    }

    public void sendMail(Long userId, String subject, String body) {
//        Optional<User> optionalUser = userRepository.findById(userId);
//        if (optionalUser.isPresent()) {
//            User user = optionalUser.get();
//            sendMail(user.getEmail(), subject, body);
//        }
    }
}
