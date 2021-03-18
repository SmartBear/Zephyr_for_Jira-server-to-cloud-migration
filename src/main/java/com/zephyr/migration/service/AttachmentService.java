package com.zephyr.migration.service;

import com.zephyr.migration.dto.AttachmentDTO;
import com.zephyr.migration.model.ZfjCloudAttachmentBean;
import com.zephyr.migration.utils.ApplicationConstants;

import java.io.File;
import java.util.List;

public interface AttachmentService {

    List<AttachmentDTO> getAttachmentResponse(Integer id, ApplicationConstants.ENTITY_TYPE entityType);

    File downloadExecutionAttachmentFileFromZFJ(String fileId, String fileName);

    ZfjCloudAttachmentBean addAttachmentInCloud(File attachment, String cloudExecutionId, String projectId, String entityName, String entityId) throws Exception;

}
