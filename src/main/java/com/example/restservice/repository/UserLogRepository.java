package com.example.restservice.repository;

import com.example.restservice.model.User;
import com.example.restservice.model.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {
    List<UserLog> findByUser(User user);
    List<UserLog> findByUserAndTimeBetween(User user, LocalDateTime startTime, LocalDateTime endTime);
    List<UserLog> findByTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}