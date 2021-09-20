package com.zephyr.migration.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {

    private IssueType issuetype;
    private List<Component> components;
    private String description;
    private Project project;
    private List<JiraVersion> fixVersions;
    private JiraUser creator;
    private JiraUser reporter;
    private Priority priority;
    private Date created;
    private Date updated;
    private String summary;
    private List<AttachmentBean> attachment;
    private IssueStatus status;

    public IssueType getIssuetype() {
        return issuetype;
    }

    public void setIssuetype(IssueType issuetype) {
        this.issuetype = issuetype;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<JiraVersion> getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(List<JiraVersion> fixVersions) {
        this.fixVersions = fixVersions;
    }

    public JiraUser getCreator() {
        return creator;
    }

    public void setCreator(JiraUser creator) {
        this.creator = creator;
    }

    public JiraUser getReporter() {
        return reporter;
    }

    public void setReporter(JiraUser reporter) {
        this.reporter = reporter;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<AttachmentBean> getAttachment() {
        return attachment;
    }

    public void setAttachment(List<AttachmentBean> attachment) {
        this.attachment = attachment;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }
}
