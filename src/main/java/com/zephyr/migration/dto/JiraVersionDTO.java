package com.zephyr.migration.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class JiraVersionDTO {
    private String self;
    private String id;
    private String description;
    private String name;
    private Boolean archived;
    private Boolean released;
    private DateTime releaseDate;
    private Boolean overdue;
    private String userReleaseDate;
    private String projectId;
    private DateTime startDate;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }

    public DateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(DateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    public String getUserReleaseDate() {
        return userReleaseDate;
    }

    public void setUserReleaseDate(String userReleaseDate) {
        this.userReleaseDate = userReleaseDate;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "JiraVersionDTO{" +
                "self='" + self + '\'' +
                ", id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", archived=" + archived +
                ", released=" + released +
                ", releaseDate=" + releaseDate +
                ", overdue=" + overdue +
                ", userReleaseDate='" + userReleaseDate + '\'' +
                ", projectId='" + projectId + '\'' +
                ", startDate=" + startDate +
                '}';
    }
}
