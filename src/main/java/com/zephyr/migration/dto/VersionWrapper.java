package com.zephyr.migration.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionWrapper {
    public String self;
    public Integer maxResults;
    public Integer startAt;
    public Integer total;
    public Boolean isLast;
    public List<JiraVersionDTO> values;

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

    public List<JiraVersionDTO> getValues() {
        return values;
    }

    public void setValues(List<JiraVersionDTO> values) {
        this.values = values;
    }
}
