package com.example.demo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void setUp() {
		taskRepository.deleteAll();
	}

	@Test
	void getAllTasks_ShouldReturnEmptyList_WhenNoTasksExist() throws Exception {
		mockMvc.perform(get("/tasks"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void getAllTasks_ShouldReturnListOfTasks_WhenTasksExist() throws Exception {
		Task task1 = new Task("First task");
		Task task2 = new Task("Second task");
		taskRepository.save(task1);
		taskRepository.save(task2);

		mockMvc.perform(get("/tasks"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].taskdescription", is("First task")))
				.andExpect(jsonPath("$[1].taskdescription", is("Second task")));
	}

	@Test
	void createTask_ShouldReturnCreatedTask_WhenValidRequest() throws Exception {
		String taskJson = "{\"taskdescription\":\"New task\"}";

		mockMvc.perform(post("/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content(taskJson))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.taskdescription", is("New task")))
				.andExpect(jsonPath("$.done", is(false)));
	}

}
