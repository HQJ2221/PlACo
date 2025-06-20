package com.example.restservice.model;

public class PostReturnData {
    private long id;

    public PostReturnData() {}

    public PostReturnData(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
