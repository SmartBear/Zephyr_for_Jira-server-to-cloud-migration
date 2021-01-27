package com.zephyr.migration.model;

public class ZfjAttachmentBean {

    private String cloudExecutionAttachmentId;
    private String cloudExecutionId;
    private String serverExecutionAttachmentId;
    private String serverExecutionId;

    public String getCloudExecutionAttachmentId() {
        return cloudExecutionAttachmentId;
    }

    public void setCloudExecutionAttachmentId(String cloudExecutionAttachmentId) {
        this.cloudExecutionAttachmentId = cloudExecutionAttachmentId;
    }

    public String getCloudExecutionId() {
        return cloudExecutionId;
    }

    public void setCloudExecutionId(String cloudExecutionId) {
        this.cloudExecutionId = cloudExecutionId;
    }

    public String getServerExecutionAttachmentId() {
        return serverExecutionAttachmentId;
    }

    public void setServerExecutionAttachmentId(String serverExecutionAttachmentId) {
        this.serverExecutionAttachmentId = serverExecutionAttachmentId;
    }

    public String getServerExecutionId() {
        return serverExecutionId;
    }

    public void setServerExecutionId(String serverExecutionId) {
        this.serverExecutionId = serverExecutionId;
    }
}
