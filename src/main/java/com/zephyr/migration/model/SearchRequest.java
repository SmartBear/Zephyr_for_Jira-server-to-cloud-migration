package com.zephyr.migration.model;

public class SearchRequest {

    private String projectId;
    private String versionId;
    private String cloudCycleId;
    private String serverCycleId;
    private String cloudVersionId;
    private String cycleName;
    private String serverFolderId;
    private String cloudFolderId;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getCloudCycleId() {
        return cloudCycleId;
    }

    public void setCloudCycleId(String cloudCycleId) {
        this.cloudCycleId = cloudCycleId;
    }

    public String getServerCycleId() {
        return serverCycleId;
    }

    public void setServerCycleId(String serverCycleId) {
        this.serverCycleId = serverCycleId;
    }

    public String getCloudVersionId() { return cloudVersionId; }

    public void setCloudVersionId(String cloudVersionId) { this.cloudVersionId = cloudVersionId; }

    public String getCycleName() { return cycleName; }

    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public String getServerFolderId() {
        return serverFolderId;
    }

    public void setServerFolderId(String serverFolderId) {
        this.serverFolderId = serverFolderId;
    }

    public String getCloudFolderId() {
        return cloudFolderId;
    }

    public void setCloudFolderId(String cloudFolderId) {
        this.cloudFolderId = cloudFolderId;
    }

    @Override
    public String toString() {
        return "SearchFolderRequest{" +
                "projectId='" + projectId + '\'' +
                ", versionId='" + versionId + '\'' +
                ", cloudCycleId='" + cloudCycleId + '\'' +
                ", serverCycleId='" + serverCycleId + '\'' +
                ", cloudVersionId='" + cloudVersionId + '\'' +
                ", cycleName='" + cycleName + '\'' +
                '}';
    }
}
