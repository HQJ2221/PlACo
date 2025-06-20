package com.example.restservice.repository;

import com.example.restservice.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUser_Id(Long userId);

    List<Submission> findByUser_IdAndAssignment_Id(Long userId, Long assignmentId);

    List<Submission> findByAssignment_Id(Long assignmentId);
}
