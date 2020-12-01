package com.zephyr.migration.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Himanshu
 */
public class CycleDTO implements Serializable {

    private static final long serialVersionUID = -7148260302926985424L;
    public String id;
    public String name;
    public String environment;
    public String build;
    public Integer versionId;
    public Integer projectId;
    public Long startDate;
    public Long endDate;
    public String description;
    public Date creationDate;

    public CycleDTO() {
    }

    public CycleDTO(String name, String environment, String build, Integer versionId, Integer projectId, Long startDate, Long endDate, String description, Integer sprintId, Date creationDate) {
        this.name = name;
        this.environment = environment;
        this.build = build;
        this.versionId = versionId;
        this.projectId = projectId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CycleDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", environment='" + environment + '\'' +
                ", build='" + build + '\'' +
                ", versionId=" + versionId +
                ", projectId=" + projectId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", description='" + description + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }
}
