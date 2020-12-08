package com.zephyr.migration.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Himanshu
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CycleDTO implements Serializable {

    private String name;
    private String id;
    private String build;
    private String environment;
    private String projectId;
    private String versionId;
    private String versionName;
    private String description;
    private String endDate;
    private String startDate;
    private Integer totalExecutions;
    private Integer totalExecuted;
    private Integer totalFolders;
    private String cloudVersionId;

    public CycleDTO() {

    }
    public CycleDTO(Integer number) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

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

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Integer getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(Integer totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public Integer getTotalExecuted() {
        return totalExecuted;
    }

    public void setTotalExecuted(Integer totalExecuted) {
        this.totalExecuted = totalExecuted;
    }

    public Integer getTotalFolders() {
        return totalFolders;
    }

    public void setTotalFolders(Integer totalFolders) {
        this.totalFolders = totalFolders;
    }

    public String getCloudVersionId() {
        return cloudVersionId;
    }

    public void setCloudVersionId(String cloudVersionId) {
        this.cloudVersionId = cloudVersionId;
    }

    @Override
    public String toString() {
        return "CycleDTO{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", build='" + build + '\'' +
                ", environment='" + environment + '\'' +
                ", projectId='" + projectId + '\'' +
                ", versionId='" + versionId + '\'' +
                ", versionName='" + versionName + '\'' +
                ", description='" + description + '\'' +
                ", endDate='" + endDate + '\'' +
                ", startDate='" + startDate + '\'' +
                ", totalExecutions=" + totalExecutions +
                ", totalExecuted=" + totalExecuted +
                ", totalFolders=" + totalFolders +
                '}';
    }
}
