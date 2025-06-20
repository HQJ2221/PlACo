package com.example.restservice.repository;

import com.example.restservice.model.SubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {
    Optional<SubmissionFile> findBySubmission_IdAndFile_Id(Long submissionId, Long fileId);

    List<SubmissionFile> findBySubmission_Id(Long submissionId);
}
