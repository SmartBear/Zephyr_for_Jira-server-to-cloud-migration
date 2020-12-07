package com.zephyr.migration.dto;

import java.io.Serializable;

/**
 * Created by Himanshu
 */
public class FolderDTO implements Serializable {

    private static final long serialVersionUID = -2279079416199904926L;
    public String id;
    public String name;
    public String description;
    public Integer versionId;
    public Integer projectId;
    public String cycleId;
    public String createdBy;
    public String createdByAccountId;
    public String modifiedBy;
    public String modifiedByAccountId;
    public Integer createdTime;
    public Integer modifiedTime;
    public Integer sprintId;

    public FolderDTO() {
    }

    public FolderDTO(String name, String description, Integer versionId, Integer projectId, String cycleId, String createdBy, String createdByAccountId, String modifiedBy, String modifiedByAccountId, Integer createdTime, Integer modifiedTime, Integer sprintId) {
        this.name = name;
        this.description = description;
        this.versionId = versionId;
        this.projectId = projectId;
        this.cycleId = cycleId;
        this.createdBy = createdBy;
        this.createdByAccountId = createdByAccountId;
        this.modifiedBy = modifiedBy;
        this.modifiedByAccountId = modifiedByAccountId;
        this.createdTime = createdTime;
        this.modifiedTime = modifiedTime;
        this.sprintId = sprintId;
    }

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

    public String getCycleId() {
        return cycleId;
    }

    public void setCycleId(String cycleId) {
        this.cycleId = cycleId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByAccountId() {
        return createdByAccountId;
    }

    public void setCreatedByAccountId(String createdByAccountId) {
        this.createdByAccountId = createdByAccountId;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedByAccountId() {
        return modifiedByAccountId;
    }

    public void setModifiedByAccountId(String modifiedByAccountId) {
        this.modifiedByAccountId = modifiedByAccountId;
    }

    public Integer getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Integer createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Integer modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getSprintId() {
        return sprintId;
    }

    public void setSprintId(Integer sprintId) {
        this.sprintId = sprintId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
