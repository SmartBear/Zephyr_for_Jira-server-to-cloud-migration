package com.zephyr.migration.dto;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * Created by Himanshu
 */
public class ExecutionCloudDTO implements Serializable {

    private static final long serialVersionUID = -7879745396141089901L;

    private String id;
    public Integer issueId;
    public Integer versionId;
    public Integer projectId;
    public String cycleId;
    public Integer orderId;
    public String comment;
    public String executedByAccountId;
    public Date executedOn;
    public String modifiedByAccountId;
    public String createdByAccountId;
    public ExecutionStatus status;
    public String cycleName;
    public String assignedToAccountId;
    public Collection<String> defects;
    public Collection<String> stepDefects;
    public Integer executionDefectCount;
    public Integer stepDefectCount;
    public Integer totalDefectCount;
    public String tenantKey;
    public String ztId;
    public Boolean  executedByZapi;
    public Date assignedOn;
    public String folderId;
    public String folderName;
    public Integer plannedExecutionTime;
    public Integer actualExecutionTime;
    private String currentStatus;

    public ExecutionCloudDTO() {
    }

    public ExecutionCloudDTO(Integer issueId, Integer versionId, Integer projectId, String cycleId, Integer orderId, String comment, String executedByAccountId, Date executedOn, String modifiedByAccountId, String createdByAccountId, ExecutionStatus status, String cycleName, String assignedToAccountId, Collection<String> defects, Collection<String> stepDefects, Integer executionDefectCount, Integer stepDefectCount, Integer totalDefectCount, String tenantKey, String ztId, Boolean executedByZapi, Date assignedOn, String folderId, String folderName, Integer plannedExecutionTime, Integer actualExecutionTime) {
        this.issueId = issueId;
        this.versionId = versionId;
        this.projectId = projectId;
        this.cycleId = cycleId;
        this.orderId = orderId;
        this.comment = comment;
        this.executedByAccountId = executedByAccountId;
        this.executedOn = executedOn;
        this.modifiedByAccountId = modifiedByAccountId;
        this.createdByAccountId = createdByAccountId;
        this.status = status;
        this.cycleName = cycleName;
        this.assignedToAccountId = assignedToAccountId;
        this.defects = defects;
        this.stepDefects = stepDefects;
        this.executionDefectCount = executionDefectCount;
        this.stepDefectCount = stepDefectCount;
        this.totalDefectCount = totalDefectCount;
        this.tenantKey = tenantKey;
        this.ztId = ztId;
        this.executedByZapi = executedByZapi;
        this.assignedOn = assignedOn;
        this.folderId = folderId;
        this.folderName = folderName;
        this.plannedExecutionTime = plannedExecutionTime;
        this.actualExecutionTime = actualExecutionTime;
    }

    public Integer getIssueId() {
        return issueId;
    }

    public void setIssueId(Integer issueId) {
        this.issueId = issueId;
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

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExecutedByAccountId() {
        return executedByAccountId;
    }

    public void setExecutedByAccountId(String executedByAccountId) {
        this.executedByAccountId = executedByAccountId;
    }

    public Date getExecutedOn() {
        return executedOn;
    }

    public void setExecutedOn(Date executedOn) {
        this.executedOn = executedOn;
    }

    public String getModifiedByAccountId() {
        return modifiedByAccountId;
    }

    public void setModifiedByAccountId(String modifiedByAccountId) {
        this.modifiedByAccountId = modifiedByAccountId;
    }

    public String getCreatedByAccountId() {
        return createdByAccountId;
    }

    public void setCreatedByAccountId(String createdByAccountId) {
        this.createdByAccountId = createdByAccountId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getCycleName() {
        return cycleName;
    }

    public void setCycleName(String cycleName) {
        this.cycleName = cycleName;
    }

    public String getAssignedToAccountId() {
        return assignedToAccountId;
    }

    public void setAssignedToAccountId(String assignedToAccountId) {
        this.assignedToAccountId = assignedToAccountId;
    }

    public Collection<String> getDefects() {
        return defects;
    }

    public void setDefects(Collection<String> defects) {
        this.defects = defects;
    }

    public Collection<String> getStepDefects() {
        return stepDefects;
    }

    public void setStepDefects(Collection<String> stepDefects) {
        this.stepDefects = stepDefects;
    }

    public Integer getExecutionDefectCount() {
        return executionDefectCount;
    }

    public void setExecutionDefectCount(Integer executionDefectCount) {
        this.executionDefectCount = executionDefectCount;
    }

    public Integer getStepDefectCount() {
        return stepDefectCount;
    }

    public void setStepDefectCount(Integer stepDefectCount) {
        this.stepDefectCount = stepDefectCount;
    }

    public Integer getTotalDefectCount() {
        return totalDefectCount;
    }

    public void setTotalDefectCount(Integer totalDefectCount) {
        this.totalDefectCount = totalDefectCount;
    }

    public String getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(String tenantKey) {
        this.tenantKey = tenantKey;
    }

    public String getZtId() {
        return ztId;
    }

    public void setZtId(String ztId) {
        this.ztId = ztId;
    }

    public Boolean getExecutedByZapi() {
        return executedByZapi;
    }

    public void setExecutedByZapi(Boolean executedByZapi) {
        this.executedByZapi = executedByZapi;
    }

    public Date getAssignedOn() {
        return assignedOn;
    }

    public void setAssignedOn(Date assignedOn) {
        this.assignedOn = assignedOn;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public Integer getPlannedExecutionTime() {
        return plannedExecutionTime;
    }

    public void setPlannedExecutionTime(Integer plannedExecutionTime) {
        this.plannedExecutionTime = plannedExecutionTime;
    }

    public Integer getActualExecutionTime() {
        return actualExecutionTime;
    }

    public void setActualExecutionTime(Integer actualExecutionTime) {
        this.actualExecutionTime = actualExecutionTime;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ExecutionDTO{" +
                "id='" + id + '\'' +
                ", issueId=" + issueId +
                ", versionId=" + versionId +
                ", projectId=" + projectId +
                ", cycleId='" + cycleId + '\'' +
                ", orderId=" + orderId +
                ", comment='" + comment + '\'' +
                ", executedByAccountId='" + executedByAccountId + '\'' +
                ", executedOn=" + executedOn +
                ", modifiedByAccountId='" + modifiedByAccountId + '\'' +
                ", createdByAccountId='" + createdByAccountId + '\'' +
                ", status=" + status +
                ", cycleName='" + cycleName + '\'' +
                ", assignedToAccountId='" + assignedToAccountId + '\'' +
                ", defects=" + defects +
                ", stepDefects=" + stepDefects +
                ", executionDefectCount=" + executionDefectCount +
                ", stepDefectCount=" + stepDefectCount +
                ", totalDefectCount=" + totalDefectCount +
                ", tenantKey='" + tenantKey + '\'' +
                ", ztId='" + ztId + '\'' +
                ", executedByZapi=" + executedByZapi +
                ", assignedOn=" + assignedOn +
                ", folderId='" + folderId + '\'' +
                ", folderName='" + folderName + '\'' +
                ", plannedExecutionTime=" + plannedExecutionTime +
                ", actualExecutionTime=" + actualExecutionTime +
                ", currentStatus='" + currentStatus + '\'' +
                '}';
    }
}
