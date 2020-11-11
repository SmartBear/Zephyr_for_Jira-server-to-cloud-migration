package com.zephyr.migration.controllers;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.zephyr.migration.dto.JiraIssueDTO;
import com.zephyr.migration.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class TestController {

    @Autowired
    TestService testService;

    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("/getIssueDescription")
    public String getIssueDescription(@RequestParam(value = "issueKey", defaultValue = "World") String issueKey) {
        Issue issue = testService.getIssueDetailsFromServer(issueKey);
       /* if(Objects.nonNull(issue)) {
            JiraIssueDTO issueDTO = testService.createIssueInCloud(issue);
        }*/
        return String.format("Issue Summary ::: %s!", issue.getSummary());
    }

    @GetMapping("/createIssueDescription")
    public String createIssueDescription(@RequestParam(value = "issueKey", defaultValue = "World") String issueKey) {
        Issue issue = testService.getIssueDetailsFromServer(issueKey);
       if(Objects.nonNull(issue)) {
            JiraIssueDTO issueDTO = testService.createIssueInCloud(issue);
       }
        return String.format("Issue Summary ::: %s!", issue.getSummary());
    }
}
