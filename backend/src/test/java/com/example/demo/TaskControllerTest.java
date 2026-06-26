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
	void getAllTasksV1_ShouldReturnEmptyList_WhenNoTasksExist() throws Exception {
		mockMvc.perform(get("/tasks").param("api", "v1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void getAllTasksV1_ShouldReturnListOfTasks_WhenTasksExist() throws Exception {
		taskRepository.save(new Task("First task"));
		taskRepository.save(new Task("Second task"));

		mockMvc.perform(get("/tasks").param("api", "v1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].taskdescription", is("First task")))
				.andExpect(jsonPath("$[1].taskdescription", is("Second task")));
	}

	@Test
	void createTaskV1_ShouldReturnCreatedTask_WhenValidRequest() throws Exception {
		String taskJson = "{\"taskdescription\":\"New task\"}";

		mockMvc.perform(post("/tasks")
				.param("api", "v1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(taskJson))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.taskdescription", is("New task")))
				.andExpect(jsonPath("$.done", is(false)));
	}

	@Test
	void getAllTasksV1_ShouldDefaultToV1_WhenApiParamMissing() throws Exception {
		taskRepository.save(new Task("Default version task"));

		mockMvc.perform(get("/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].taskdescription", is("Default version task")));
	}

	@Test
	void getAllTasksV2_ShouldReturnWrappedList_WhenNoTasksExist() throws Exception {
		mockMvc.perform(get("/tasks").param("api", "v2"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.apiVersion", is("v2")))
				.andExpect(jsonPath("$.count", is(0)))
				.andExpect(jsonPath("$.items", hasSize(0)));
	}

	@Test
	void getAllTasksV2_ShouldReturnWrappedListWithDescriptionField_WhenTasksExist() throws Exception {
		taskRepository.save(new Task("First task"));
		taskRepository.save(new Task("Second task"));

		mockMvc.perform(get("/tasks").param("api", "v2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.apiVersion", is("v2")))
				.andExpect(jsonPath("$.count", is(2)))
				.andExpect(jsonPath("$.items", hasSize(2)))
				.andExpect(jsonPath("$.items[0].description", is("First task")))
				.andExpect(jsonPath("$.items[1].description", is("Second task")));
	}

	@Test
	void createTaskV2_ShouldReturnTaskV2Dto_WhenValidRequest() throws Exception {
		String taskJson = "{\"taskdescription\":\"New v2 task\"}";

		mockMvc.perform(post("/tasks")
				.param("api", "v2")
				.contentType(MediaType.APPLICATION_JSON)
				.content(taskJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.description", is("New v2 task")))
				.andExpect(jsonPath("$.done", is(false)));
	}

	@Test
	void deleteTaskV2_ShouldReturnJsonAction_WhenTaskExists() throws Exception {
		taskRepository.save(new Task("Delete me"));

		mockMvc.perform(post("/delete")
				.param("api", "v2")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"taskdescription\":\"Delete me\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.apiVersion", is("v2")))
				.andExpect(jsonPath("$.action", is("deleted")))
				.andExpect(jsonPath("$.description", is("Delete me")));
	}

	@Test
	void toggleDoneV2_ShouldReturnJsonAction_WhenTaskExists() throws Exception {
		taskRepository.save(new Task("Toggle me"));

		mockMvc.perform(post("/toggle-done")
				.param("api", "v2")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"taskdescription\":\"Toggle me\",\"done\":true}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.apiVersion", is("v2")))
				.andExpect(jsonPath("$.action", is("toggled")))
				.andExpect(jsonPath("$.done", is(true)));
	}

	@Test
	void getAllTasks_ShouldReturnBadRequest_WhenApiVersionInvalid() throws Exception {
		mockMvc.perform(get("/tasks").param("api", "v99"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Unsupported API version")));
	}

}
