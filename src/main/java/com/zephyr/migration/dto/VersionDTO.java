package com.zephyr.migration.dto;

/**
 * Created by Himanshu Singhal on 26-11-2020.
 */
public class VersionDTO {

    public String name;
    public String description;
    public Long projectId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
