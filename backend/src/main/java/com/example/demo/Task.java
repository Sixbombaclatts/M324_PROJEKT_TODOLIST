package com.example.demo;

/** the simplest task 
 * 
 * @author luh
 */
public class Task {
	
	private String taskdescription; // must have the EXACT name as his React state property and may not be ignored!
	private String dueDate;
	private boolean reminderEnabled;
	private boolean done;

	public Task() {
		// default constructor required for Jackson deserialization
    }

	public String getTaskdescription() { // do not apply camel-case here! Its a Bean!
		return taskdescription;
	}

	public void setTaskdescription(String taskdescription) { // do not apply camel-case here! Its a Bean!
		this.taskdescription = taskdescription;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public boolean getReminderEnabled() {
		return reminderEnabled;
	}

	public void setReminderEnabled(boolean reminderEnabled) {
		this.reminderEnabled = reminderEnabled;
	}

	public boolean getDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

}