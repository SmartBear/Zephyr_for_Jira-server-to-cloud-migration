package com.zephyr.migration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZfjCloudStepResultUpdateBean {

    private String stepResultId;
    private String executionId;
    private String statusId;
    private String comment;

    public String getStepResultId() {
        return stepResultId;
    }

    public void setStepResultId(String stepResultId) {
        this.stepResultId = stepResultId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
