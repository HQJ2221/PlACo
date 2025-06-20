package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.Role;
import com.example.restservice.model.Schedule;
import com.example.restservice.model.User;
import com.example.restservice.repository.ScheduleRepository;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.service.LogService;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class ScheduleControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogService logService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("User Can Create Schedule")
    public void testCreateSchedule() {
        Assertions.assertDoesNotThrow(() -> {
            User user = TestUtil.generateRandomUser();
            userRepository.save(user);

            JSONObject userJson = new JSONObject();
            userJson.put("id", user.getId());
            JSONObject scheduleJson = new JSONObject();
            scheduleJson.put("user", userJson);
            scheduleJson.put("time", LocalDateTime.now());
            scheduleJson.put("createTime", LocalDateTime.now());
            scheduleJson.put("title", TestUtil.generateRandomString());
            scheduleJson.put("description", TestUtil.generateRandomString());

            mockMvc.perform(post("/schedules")
                            .with(user(user))
                            .content(scheduleJson.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Admin Can Get All Schedules")
    public void testGetAllSchedulesAsAdmin() {
        Assertions.assertDoesNotThrow(() -> {
            User admin = TestUtil.generateRandomUser();
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            Schedule schedule = new Schedule();
            schedule.setUser(admin);
            schedule.setCreateTime(LocalDateTime.now());
            schedule.setTitle(TestUtil.generateRandomString());
            schedule.setDescription(TestUtil.generateRandomString());
            scheduleRepository.save(schedule);

            mockMvc.perform(get("/schedules")
                            .with(user(admin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").isNumber())
                    .andExpect(jsonPath("$.data[0].title").isString())
                    .andExpect(jsonPath("$.data[0].description").isString())
                    .andReturn();
        });
    }

    @Test
    @DisplayName("User Can Get Their Own Schedule By ID")
    public void testGetScheduleById() {
        Assertions.assertDoesNotThrow(() -> {
            User user = TestUtil.generateRandomUser();
            userRepository.save(user);

            Schedule schedule = new Schedule();
            schedule.setUser(user);
            schedule.setCreateTime(LocalDateTime.now());
            schedule.setTitle(TestUtil.generateRandomString());
            schedule.setDescription(TestUtil.generateRandomString());
            schedule = scheduleRepository.save(schedule);

            mockMvc.perform(get("/schedules/{id}", schedule.getId())
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.title").value(schedule.getTitle()))
                    .andExpect(jsonPath("$.data.description").value(schedule.getDescription()))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("User Can Update Their Own Schedule")
    public void testUpdateSchedule() {
        Assertions.assertDoesNotThrow(() -> {
            User user = TestUtil.generateRandomUser();
            userRepository.save(user);

            Schedule schedule = new Schedule();
            schedule.setUser(user);
            schedule.setCreateTime(LocalDateTime.now());
            schedule.setTitle("Original Title");
            schedule.setDescription("Original Description");
            schedule = scheduleRepository.save(schedule);

            JSONObject json = new JSONObject();
            json.put("title", TestUtil.generateRandomString());

            mockMvc.perform(put("/schedules/" + schedule.getId())
                            .with(user(user))
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.title").value(json.getString("title")))
                    .andExpect(jsonPath("$.data.description").value(schedule.getDescription()))
                    .andReturn();
        });

    }

    @Test
    @DisplayName("User Can Delete Their Own Schedule")
    public void testDeleteSchedule() {
        Assertions.assertDoesNotThrow(() -> {
            User user = TestUtil.generateRandomUser();
            userRepository.save(user);

            Schedule schedule = new Schedule();
            schedule.setUser(user);
            schedule.setCreateTime(LocalDateTime.now());
            schedule.setTitle(TestUtil.generateRandomString());
            schedule.setDescription(TestUtil.generateRandomString());
            schedule = scheduleRepository.save(schedule);

            mockMvc.perform(delete("/schedules/{id}", schedule.getId())
                            .with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andReturn();
        });
    }
}
