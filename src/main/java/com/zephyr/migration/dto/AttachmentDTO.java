package com.zephyr.migration.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AttachmentDTO {

    private String fileName;
    private String fileId;
    private String fileSize;
    private String contentType;
    private String fileIcon;
    private String author;
    private String comment;
    private String dateCreated;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileIcon() {
        return fileIcon;
    }

    public void setFileIcon(String fileIcon) {
        this.fileIcon = fileIcon;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        return "ExecutionAttachmentDTO{" +
                "fileName='" + fileName + '\'' +
                ", fileId='" + fileId + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileIcon='" + fileIcon + '\'' +
                ", author='" + author + '\'' +
                ", comment='" + comment + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                '}';
    }
}
