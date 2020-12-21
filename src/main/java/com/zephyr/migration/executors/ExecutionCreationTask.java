package com.zephyr.migration.executors;

import com.zephyr.migration.dto.ExecutionDTO;
import com.zephyr.migration.model.SearchRequest;
import com.zephyr.migration.model.ZfjCloudExecutionBean;
import com.zephyr.migration.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class ExecutionCreationTask implements Callable<Map<ExecutionDTO, ZfjCloudExecutionBean>> {

    private final List<ExecutionDTO> executionDTOList;
    private final SearchRequest searchRequest;

    @Autowired
    ExecutionService executionService;

    public ExecutionCreationTask(List<ExecutionDTO> executionDTOList, SearchRequest searchRequest) {
        this.executionDTOList = executionDTOList;
        this.searchRequest = searchRequest;
    }


    @Override
    public Map<ExecutionDTO, ZfjCloudExecutionBean> call() throws Exception {
        Map<ExecutionDTO, ZfjCloudExecutionBean> serverCloudExecutionMapping = new HashMap<>();
        if(executionDTOList.size() > 0) {
            executionDTOList.parallelStream().forEachOrdered(serverExecution -> {
                ZfjCloudExecutionBean zfjCloudExecutionBean = executionService.createExecutionInJiraCloud(prepareRequestForCloud(serverExecution, searchRequest));
                if(Objects.nonNull(zfjCloudExecutionBean)) {
                    serverCloudExecutionMapping.put(serverExecution,zfjCloudExecutionBean);
                }
            });
        }
        return serverCloudExecutionMapping;
    }

    private ZfjCloudExecutionBean prepareRequestForCloud(ExecutionDTO serverExecution, SearchRequest searchRequest) {
        ZfjCloudExecutionBean zfjCloudExecutionBean = new ZfjCloudExecutionBean();

        zfjCloudExecutionBean.setProjectId(Integer.parseInt(searchRequest.getProjectId()));
        zfjCloudExecutionBean.setVersionId(Integer.parseInt(searchRequest.getCloudVersionId()));
        zfjCloudExecutionBean.setCycleId(searchRequest.getCloudCycleId());
        zfjCloudExecutionBean.setIssueId(serverExecution.getIssueId());
        zfjCloudExecutionBean.setExecutedByZapi(Boolean.TRUE);
        if(Objects.nonNull(searchRequest.getCloudFolderId())) {
            zfjCloudExecutionBean.setFolderId(searchRequest.getCloudFolderId());
        }
        zfjCloudExecutionBean.setComment("created using zephyr server - cloud migration.");

        return zfjCloudExecutionBean;
    }
}
