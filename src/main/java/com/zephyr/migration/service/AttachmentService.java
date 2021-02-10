package com.zephyr.migration.service;

import com.zephyr.migration.dto.ExecutionAttachmentDTO;
import com.zephyr.migration.model.ZfjAttachmentBean;
import com.zephyr.migration.utils.ApplicationConstants;

import java.io.File;
import java.util.List;

public interface AttachmentService {

    List<ExecutionAttachmentDTO> getAttachmentResponse(Integer id, ApplicationConstants.ENTITY_TYPE entityType);

    File downloadExecutionAttachmentFileFromZFJ(String fileId, String fileName);

    ZfjAttachmentBean addAttachmentInCloud(File attachment, String cloudExecutionId, String projectId, String entityName, String entityId) throws Exception;
}