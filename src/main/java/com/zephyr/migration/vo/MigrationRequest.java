package com.zephyr.migration.vo;

public class MigrationRequest {

    private Long projectId;
    private String listOfProjects;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getListOfProjects() {
        return listOfProjects;
    }

    public void setListOfProjects(String listOfProjects) {
        this.listOfProjects = listOfProjects;
    }
}
