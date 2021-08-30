package com.zephyr.migration.model;

public class DefectLinkResponseBean {

    private String serverExecutionId;
    private String cloudExecutionId;
    private String serverDefectLinks;
    private String cloudDefectLinks;
    private String serverStepResultId;
    private String cloudStepResultId;

    public String getServerExecutionId() {
        return serverExecutionId;
    }

    public void setServerExecutionId(String serverExecutionId) {
        this.serverExecutionId = serverExecutionId;
    }

    public String getCloudExecutionId() {
        return cloudExecutionId;
    }

    public void setCloudExecutionId(String cloudExecutionId) {
        this.cloudExecutionId = cloudExecutionId;
    }

    public String getServerDefectLinks() {
        return serverDefectLinks;
    }

    public void setServerDefectLinks(String serverDefectLinks) {
        this.serverDefectLinks = serverDefectLinks;
    }

    public String getCloudDefectLinks() {
        return cloudDefectLinks;
    }

    public void setCloudDefectLinks(String cloudDefectLinks) {
        this.cloudDefectLinks = cloudDefectLinks;
    }

    public String getServerStepResultId() {
        return serverStepResultId;
    }

    public void setServerStepResultId(String serverStepResultId) {
        this.serverStepResultId = serverStepResultId;
    }

    public String getCloudStepResultId() {
        return cloudStepResultId;
    }

    public void setCloudStepResultId(String cloudStepResultId) {
        this.cloudStepResultId = cloudStepResultId;
    }
}
