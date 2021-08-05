package com.zephyr.migration.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by dubey on 11-05-2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionWrapper {
    private String self;
    private Integer maxResults;
    private Integer startAt;
    private Integer total;
    private Boolean isLast;
    private List<JiraVersion> values;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Integer getStartAt() {
        return startAt;
    }

    public void setStartAt(Integer startAt) {
        this.startAt = startAt;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Boolean getLast() {
        return isLast;
    }

    public void setLast(Boolean last) {
        isLast = last;
    }

    public List<JiraVersion> getValues() {
        return values;
    }

    public void setValues(List<JiraVersion> values) {
        this.values = values;
    }
}
