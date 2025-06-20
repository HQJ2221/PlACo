package com.example.restservice.repository;

import com.example.restservice.model.CourseUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseUserRepository extends JpaRepository<CourseUser, Long> {
    Optional<CourseUser> findCourseUserByCourse_IdAndUser_Id(Long courseId, Long userId);

    List<CourseUser> findByCourse_Id(Long courseId);

    List<CourseUser> findByUser_Id(Long userId);

    boolean existsByCourse_IdAndUser_Id(Long courseId, Long userId);
}
