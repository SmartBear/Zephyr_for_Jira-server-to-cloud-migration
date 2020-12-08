package com.zephyr.migration.model;

public class SearchFolderRequest {

    private String projectId;
    private String versionId;
    private String cloudCycleId;
    private String serverCycleId;

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
}
