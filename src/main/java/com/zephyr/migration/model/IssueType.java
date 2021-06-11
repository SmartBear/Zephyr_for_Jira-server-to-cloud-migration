package com.zephyr.migration.model;



import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class IssueType  {
	// Default issue type
	public static final String ISSUE_TYPE_BUG = "Bug";
	public static final String ISSUE_TYPE_TASK = "Test";
	public static final String ISSUE_TYPE_IMPROVEMENT = "Improvement";
	public static final String ISSUE_TYPE_SUBTASK = "SubTask";
	public static final String ISSUE_TYPE_NEW_FEATURE = "New Feature";
	
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getSubtask() {
		return subtask;
	}
	public void setSubtask(Boolean subtask) {
		this.subtask = subtask;
	}
	public Integer getAvatarId() {
		return avatarId;
	}
	public void setAvatarId(Integer avatarId) {
		this.avatarId = avatarId;
	}
	public static String getIssueTypeBug() {
		return ISSUE_TYPE_BUG;
	}
	public static String getIssueTypeTask() {
		return ISSUE_TYPE_TASK;
	}
	public static String getIssueTypeImprovement() {
		return ISSUE_TYPE_IMPROVEMENT;
	}
	public static String getIssueTypeSubtask() {
		return ISSUE_TYPE_SUBTASK;
	}
	public static String getIssueTypeNewFeature() {
		return ISSUE_TYPE_NEW_FEATURE;
	}
	private String self;
	private String id;
	private String description;
	private String iconUrl;
	private String name;
	private Boolean subtask;
	private Integer avatarId;
}
