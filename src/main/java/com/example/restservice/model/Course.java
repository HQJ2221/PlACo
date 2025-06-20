package com.example.restservice.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
    private List<CourseUser> courseUsers;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
    private Set<Assignment> assignments;

    public void updateWithoutId(Course courseDetails) {
        if (courseDetails.getName() != null) {
            this.name = courseDetails.getName();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
