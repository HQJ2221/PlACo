package com.example.restservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courses_users", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "user_id"}))
public class CourseUser {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Course course;

    @ManyToOne
    private User user;

    // 使用枚举类型 Role，并映射到数据库中的 "role" 列
    @Enumerated(EnumType.STRING)  // 使用 STRING 方式存储枚举值（存储为 "TEACHER" 或 "STUDENT"）
    @Column(name = "role", nullable = false)  // 映射到数据库中的 "role" 列，不允许为空
    private RoleInCourse role;

    public Long getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public User getUser() {
        return user;
    }

    public RoleInCourse getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setRoleInCourse(RoleInCourse role) {
        this.role = role;
    }
}
