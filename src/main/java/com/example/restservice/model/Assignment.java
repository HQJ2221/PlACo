package com.example.restservice.model;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "assignments")
public class Assignment {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user; // 用户

    @ManyToOne
    private Course course; // 课程

    private LocalDateTime dueDate; // 当前作业的ddl

    private Integer fullMark; //作业满分

    private LocalDateTime publishTime; //作业发布时间（对学生可见时间）

    private String title; //作业标题

    private String description; //作业描述

    private Integer maxAttempts; //提交次数限制

    private AssignmentType type; //作业类别，可以为代码作业，非代码作业，代码作业需要指定语言

    private LocalDateTime createTime; //作业创建时间

    @Column(nullable = false)
    private Boolean needOCR = false; //作业是否需要OCR处理，OCR微服务需要将文件保存为id

    // code
    @ElementCollection()
    @Enumerated(EnumType.STRING)
    Set<ProgrammingLanguage> programmingLanguages;


//    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OrderColumn
//    private Set<TestCase> testCases;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "assignment")
    private Set<TestCase> testCases;

    // 与 AssignmentFile 的关系
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "assignment")
    private Set<AssignmentFile> assignmentFiles;

    // 与 Submission 的关系
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "assignment")
    private Set<Submission> submissions;

    public void updateWithoutId(Assignment assignmentDetails) {
        if (assignmentDetails.getDueDate() != null) {
            this.dueDate = assignmentDetails.getDueDate();
        }
        if (assignmentDetails.getFullMark() != null) {
            this.fullMark = assignmentDetails.getFullMark();
        }
        if (assignmentDetails.getPublishTime() != null) {
            this.publishTime = assignmentDetails.getPublishTime();
        }
        if (assignmentDetails.getTitle() != null) {
            this.title = assignmentDetails.getTitle();
        }
        if (assignmentDetails.getDescription() != null) {
            this.description = assignmentDetails.getDescription();
        }
        if (assignmentDetails.getMaxAttempts() != null) {
            this.maxAttempts = assignmentDetails.getMaxAttempts();
        }
        if (assignmentDetails.getType() != null) {
            this.type = assignmentDetails.getType();
        }

        this.needOCR=assignmentDetails.getNeedOCR();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime deadline) {
        this.dueDate = deadline;
    }

    public Integer getFullMark() {
        return fullMark;
    }

    public void setFullMark(Integer fullMark) {
        this.fullMark = fullMark;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer attemptNumberLimitation) {
        this.maxAttempts = attemptNumberLimitation;
    }

    public AssignmentType getType() {
        return type;
    }

    public void setType(AssignmentType type) {
        this.type = type;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public boolean getNeedOCR(){
        return needOCR;
    }

    public void setNeedOCR(boolean needOCR){
        this.needOCR=needOCR;
    }

    // code

    public Set<ProgrammingLanguage> getProgrammingLanguages() {
        return programmingLanguages;
    }

    public void setProgrammingLanguages(Set<ProgrammingLanguage> programmingLanguages) {
        this.programmingLanguages = programmingLanguages;
    }
}