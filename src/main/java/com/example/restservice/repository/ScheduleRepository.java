package com.example.restservice.repository;

import com.example.restservice.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>{
    List<Schedule> findByUser_Id(Long userId);
}
