package com.example.restservice.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue
    private Long id; //提交文件的id

    @Column(nullable = false)
    private String originalName; //提交作业的原名

    @Column(unique = true, nullable = false)
    private String fileName; //重命名的名称

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "file")
    private Set<AssignmentFile> assignmentFiles; //与AssignmentFile的关系

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "file")
    private Set<SubmissionFile> submissionsFiles; //与Submission的关系

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}