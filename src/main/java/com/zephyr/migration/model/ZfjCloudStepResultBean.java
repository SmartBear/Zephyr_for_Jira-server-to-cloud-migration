package com.zephyr.migration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZfjCloudStepResultBean {

    private String id;
    private Long executedOn;
    private String comment;
    private String executedBy;
    private String executedByAccountId;
    private String executionId;
    private String stepId;
    private ExecutionStatus status;
    private Long issueId;
    private Long issueIndex;
    private String createdBy;
    private String createdByAccountId;
    private String modifiedBy;
    private String modifiedByAccountId;
    private String executionStatusIndex;
    private String executionIndex;
    private Integer orderId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getExecutedOn() {
        return executedOn;
    }

    public void setExecutedOn(Long executedOn) {
        this.executedOn = executedOn;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public String getExecutedByAccountId() {
        return executedByAccountId;
    }

    public void setExecutedByAccountId(String executedByAccountId) {
        this.executedByAccountId = executedByAccountId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
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

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Long getIssueIndex() {
        return issueIndex;
    }

    public void setIssueIndex(Long issueIndex) {
        this.issueIndex = issueIndex;
    }

    public String getExecutionStatusIndex() {
        return executionStatusIndex;
    }

    public void setExecutionStatusIndex(String executionStatusIndex) {
        this.executionStatusIndex = executionStatusIndex;
    }

    public String getExecutionIndex() {
        return executionIndex;
    }

    public void setExecutionIndex(String executionIndex) {
        this.executionIndex = executionIndex;
    }
}
