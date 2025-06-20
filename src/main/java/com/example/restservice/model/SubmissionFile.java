package com.example.restservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_files", uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id", "file_id"}))
public class SubmissionFile {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Submission submission;

    @ManyToOne
    private File file;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission){
        this.submission=submission;
    }

    public File getFile(){
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
