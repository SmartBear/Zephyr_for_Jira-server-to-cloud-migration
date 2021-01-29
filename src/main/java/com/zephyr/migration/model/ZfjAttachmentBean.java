package com.zephyr.migration.model;

public class ZfjAttachmentBean {

    private String cloudExecutionAttachmentId;
    private String cloudExecutionId;
    private String serverExecutionAttachmentId;
    private String serverExecutionId;
    private Integer serverStepResultId;
    private String fileId;
    private String fileName;

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

    public Integer getServerStepResultId() {
        return serverStepResultId;
    }

    public void setServerStepResultId(Integer serverStepResultId) {
        this.serverStepResultId = serverStepResultId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
