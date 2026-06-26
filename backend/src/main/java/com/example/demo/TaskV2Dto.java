package com.example.demo;

public class TaskV2Dto {

	private Long id;
	private String description;
	private String dueDate;
	private boolean reminderEnabled;
	private boolean done;

	public static TaskV2Dto from(Task task) {
		TaskV2Dto dto = new TaskV2Dto();
		dto.id = task.getId();
		dto.description = task.getTaskdescription();
		dto.dueDate = task.getDueDate();
		dto.reminderEnabled = task.getReminderEnabled();
		dto.done = task.getDone();
		return dto;
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getDueDate() {
		return dueDate;
	}

	public boolean isReminderEnabled() {
		return reminderEnabled;
	}

	public boolean isDone() {
		return done;
	}

}
