package com.zephyr.migration.model;

import java.io.Serializable;


public class ExecutionStatus implements Serializable {

    private static final long serialVersionUID = -659066232549849610L;
    private Long id;
    private String name;
    private String color;
    private String type;

    public ExecutionStatus() {
    }

    public ExecutionStatus(Long id, String name, String color, String type) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.type = type;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
