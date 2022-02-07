package com.zephyr.migration.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TestStepResultDTO implements Comparable<TestStepResultDTO> {

    private Integer id;
    private Long executedOn;
    private String status;
    private String comment;
    private String executedBy;
    private Integer executionId;
    private Integer stepId;
    private List<String> defectList;
   // private List<Status> executionStatus;
    private Integer issueId;
    private List<Map<String, String>> defects;
    private String updateDefectList;
    private Integer projectId;
    private Integer orderId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getExecutedOn() {
        return executedOn;
    }

    public void setExecutedOn(Long executedOn) {
        this.executedOn = executedOn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Integer getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Integer executionId) {
        this.executionId = executionId;
    }

    public Integer getStepId() {
        return stepId;
    }

    public void setStepId(Integer stepId) {
        this.stepId = stepId;
    }

    public List<String> getDefectList() {
        return defectList;
    }

    public void setDefectList(List<String> defectList) {
        this.defectList = defectList;
    }

    public Integer getIssueId() {
        return issueId;
    }

    public void setIssueId(Integer issueId) {
        this.issueId = issueId;
    }

    public List<Map<String, String>> getDefects() {
        return defects;
    }

    public void setDefects(List<Map<String, String>> defects) {
        this.defects = defects;
    }

    public String getUpdateDefectList() {
        return updateDefectList;
    }

    public void setUpdateDefectList(String updateDefectList) {
        this.updateDefectList = updateDefectList;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    @Override
    public int compareTo(TestStepResultDTO stepResult) {
        //Ascending order sorting.
        return this.id - stepResult.id;
    }

    @Override
    public String toString() {
        return "TestStepResultDTO{" +
                "id=" + id +
                ", executionId=" + executionId +
                ", stepId=" + stepId +
                ", defectList=" + defectList +
                ", issueId=" + issueId +
                ", defects=" + defects +
                '}';
    }
}
