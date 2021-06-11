package com.zephyr.migration.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Component {
    public Component() {

    }

    public Component(String name) {
        this.name = name;
    }

    public Component(String id, String name) {
        this.id = id;
        this.name = name;
    }

    private String self;
    private String id;
    private String name;
    private String description;

    private Map<String, String> lead;
    private String displayName;
    private Boolean active;

    private Boolean isAssigneeTypeValid;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getLead() {
        return lead;
    }

    public void setLead(Map<String, String> lead) {
        this.lead = lead;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getAssigneeTypeValid() {
        return isAssigneeTypeValid;
    }

    public void setAssigneeTypeValid(Boolean assigneeTypeValid) {
        isAssigneeTypeValid = assigneeTypeValid;
    }
}
