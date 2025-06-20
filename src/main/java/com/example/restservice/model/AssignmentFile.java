package com.example.restservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "assignments_files", uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "file_id"}))
public class AssignmentFile {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Assignment assignment;

    @ManyToOne
    private File file;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment){
        this.assignment=assignment;
    }

    public File getFile(){
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
