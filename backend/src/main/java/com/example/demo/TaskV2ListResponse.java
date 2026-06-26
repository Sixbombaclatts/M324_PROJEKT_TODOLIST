package com.example.demo;

import java.util.List;

public class TaskV2ListResponse {

	private final String apiVersion = "v2";
	private final int count;
	private final List<TaskV2Dto> items;

	public TaskV2ListResponse(List<Task> tasks) {
		this.items = tasks.stream().map(TaskV2Dto::from).toList();
		this.count = this.items.size();
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public int getCount() {
		return count;
	}

	public List<TaskV2Dto> getItems() {
		return items;
	}

}
