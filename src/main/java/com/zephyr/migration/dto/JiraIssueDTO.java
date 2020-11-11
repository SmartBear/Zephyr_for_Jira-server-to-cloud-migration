package com.zephyr.migration.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssueDTO {
    private String status;
    private Map<String, Object> issuetype;
    private Map<String, Object> project;
    private String transitionsUri;
    private Iterable<String> expandos;
    private  List<Map<String, Object>> components;
    private String summary;
    @Nullable
    private String description;
    @Nullable
    private Map<String, Object> reporter;
    private Map<String, Object> assignee;
    @Nullable
    private  String resolution;
    private  Collection<String> issueFields;
    private  DateTime creationDate;
    private  DateTime updateDate;
    private  DateTime dueDate;
    private Map<String, Object> priority;
    private  String votes;
    @Nullable
    private  List<Map<String, Object>> versions;
    private String environment;
    private String duedate;
    @Nullable
    private   List<Map<String, Object>> fixVersions;
    @Nullable
    private  List<Map<String, Object>> affectedVersions;

    private  Collection<String> comments;


    @Nullable
    @JsonIgnore
    private  List<String> issues= Collections.emptyList();

    private  Collection<String> worklogs;
    private  String watchers;

    private List<String> labels;

    private Map<String, Object> timetracking;
    private Map<String, Object> security;

    private Map<String, Object> mandatoryCF;

    public JiraIssueDTO() {
    }

    public Map<String, Object> getMandatoryCF() {
        return mandatoryCF;
    }

    public void setMandatoryCF(Map<String, Object> mandatoryCF) {
        this.mandatoryCF = mandatoryCF;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getIssuetype() {
        return issuetype;
    }

    public void setIssuetype(Map<String, Object> issuetype) {
        this.issuetype = issuetype;
    }

    public Map<String, Object> getProject() {
        return project;
    }

    public void setProject(Map<String, Object> project) {
        this.project = project;
    }

    public String getTransitionsUri() {
        return transitionsUri;
    }

    public void setTransitionsUri(String transitionsUri) {
        this.transitionsUri = transitionsUri;
    }

    public Iterable<String> getExpandos() {
        return expandos;
    }

    public void setExpandos(Iterable<String> expandos) {
        this.expandos = expandos;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public Map<String, Object> getReporter() {
        return reporter;
    }

    public void setReporter(@Nullable Map<String, Object> reporter) {
        this.reporter = reporter;
    }

    @Nullable
    public String getResolution() {
        return resolution;
    }

    public void setResolution(@Nullable String resolution) {
        this.resolution = resolution;
    }

    public Collection<String> getIssueFields() {
        return issueFields;
    }

    public void setIssueFields(Collection<String> issueFields) {
        this.issueFields = issueFields;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(DateTime updateDate) {
        this.updateDate = updateDate;
    }

    public DateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getVotes() {
        return votes;
    }

    public void setVotes(String votes) {
        this.votes = votes;
    }

    /*@Nullable
    public List<Map<String, Object>> getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(@Nullable List<Map<String, Object>> fixVersions) {
        this.fixVersions = fixVersions;
    }*/

    @Nullable
    public List<Map<String, Object>> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(@Nullable List<Map<String, Object>> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public Collection<String> getComments() {
        return comments;
    }

    public void setComments(Collection<String> comments) {
        this.comments = comments;
    }

    public Collection<String> getWorklogs() {
        return worklogs;
    }

    public void setWorklogs(Collection<String> worklogs) {
        this.worklogs = worklogs;
    }

    public String getWatchers() {
        return watchers;
    }

    public void setWatchers(String watchers) {
        this.watchers = watchers;
    }

    public Map<String, Object> getAssignee() {
        return assignee;
    }

    public void setAssignee(Map<String, Object> assignee) {
        this.assignee = assignee;
    }

    public List<Map<String, Object>> getComponents() {
        return components;
    }

    public void setComponents(List<Map<String, Object>> components) {
        this.components = components;
    }

    public Map<String, Object> getSecurity() {
        return security;
    }

    public void setSecurity(Map<String, Object> security) {
        this.security = security;
    }

    public Map<String, Object> getTimetracking() {
        return timetracking;
    }

    public void setTimetracking(Map<String, Object> timetracking) {
        this.timetracking = timetracking;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    @Nullable
    public List<Map<String, Object>> getFixVersions() {
        return fixVersions;
    }

    public void setFixVersions(@Nullable List<Map<String, Object>> fixVersions) {
        this.fixVersions = fixVersions;
    }

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Nullable
    public List<Map<String, Object>> getVersions() {
        return versions;
    }

    public void setVersions(@Nullable List<Map<String, Object>> versions) {
        this.versions = versions;
    }

    public Map<String, Object> getPriority() {
        return priority;
    }

    public void setPriority(Map<String, Object> priority) {
        this.priority = priority;
    }

    @Nullable
    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(@Nullable List<String> issues) {
        this.issues = issues;
    }
}
