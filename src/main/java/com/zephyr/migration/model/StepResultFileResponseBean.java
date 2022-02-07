package com.zephyr.migration.model;

public class StepResultFileResponseBean {

    private String serverExecutionId;
    private String cloudExecutionId;
    private String serverStepResultId;
    private String cloudStepResultId;

    public StepResultFileResponseBean(String serverExecutionId, String cloudExecutionId, String serverStepResultId, String cloudStepResultId) {
        this.serverExecutionId = serverExecutionId;
        this.cloudExecutionId = cloudExecutionId;
        this.serverStepResultId = serverStepResultId;
        this.cloudStepResultId = cloudStepResultId;
    }

    public String getServerExecutionId() {
        return serverExecutionId;
    }

    public String getCloudExecutionId() {
        return cloudExecutionId;
    }

    public String getServerStepResultId() {
        return serverStepResultId;
    }

    public String getCloudStepResultId() {
        return cloudStepResultId;
    }
}
