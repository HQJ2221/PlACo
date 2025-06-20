package com.example.restservice.repository;

import com.example.restservice.model.AssignmentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, Long>{
    List<AssignmentFile> findByAssignment_Id(Long assignmentId);

    Optional<AssignmentFile> findByAssignment_IdAndFile_Id(Long assignmentId, Long fileId);
}
