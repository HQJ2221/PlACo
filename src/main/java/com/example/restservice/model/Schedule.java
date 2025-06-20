package com.example.restservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue
    private Long id; //日程id

    @ManyToOne
    private User user; //用户

    private LocalDateTime createTime; //创建时间

    private LocalDateTime time; //日程设定时间

    private String title; //日程标题

    private String description; //日程描述

    public void updateWithoutId(Schedule schedule){
        if(schedule.getUser() != null){
            this.user = schedule.getUser();
        }
        if(schedule.getCreateTime() != null){
            this.createTime = schedule.getCreateTime();
        }
        if(schedule.getTime() != null){
            this.time = schedule.getTime();
        }
        if(schedule.getTitle() != null){
            this.title = schedule.getTitle();
        }
        if(schedule.getDescription() != null){
            this.description = schedule.getDescription();
        }
    }

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
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
}
