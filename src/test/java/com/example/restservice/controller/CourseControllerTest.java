package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.Course;
import com.example.restservice.model.Role;
import com.example.restservice.model.User;
import com.example.restservice.repository.CourseRepository;
import com.example.restservice.repository.UserRepository;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// AI-generated-content
// tool: ChatGPT
// version: latest
// usage: generate based on other tests and manual debug
@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class CourseControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()) // Added Spring Security configuration
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("Test admin can create course")
    public void testAdminCreateCourse() throws Exception {
        assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create course JSON
            JSONObject json = new JSONObject();
            json.put("name", TestUtil.generateRandomString());

            mockMvc.perform(post("/courses")
                            .with(user(savedAdmin))
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test user can get course")
    public void testUserGetCourse() throws Exception {
        assertDoesNotThrow(() -> {
            // Create regular user
            User user = TestUtil.generateRandomUser(Role.USER);
            User savedUser = userRepository.save(user);

            // Create course
            Course course = new Course();
            course.setName(TestUtil.generateRandomString());
            Course savedCourse = courseRepository.save(course);

            // GET course
            mockMvc.perform(get("/courses/" + savedCourse.getId())
                            .with(user(savedUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").value(savedCourse.getId()))
                    .andExpect(jsonPath("$.data.name").value(course.getName()))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test admin can update course")
    public void testAdminUpdateCourse() {
        assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create course
            Course course = new Course();
            course.setName(TestUtil.generateRandomString());
            Course savedCourse = courseRepository.save(course);

            // Update course
            JSONObject json = new JSONObject();
            String newName = TestUtil.generateRandomString();
            json.put("name", newName);

            mockMvc.perform(put("/courses/" + savedCourse.getId())
                            .with(user(savedAdmin))
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").value(savedCourse.getId()))
                    .andExpect(jsonPath("$.data.name").value(newName))
                    .andReturn();

            // Check course is updated
            Optional<Course> courseOptional = courseRepository.findById(savedCourse.getId());
            Assertions.assertTrue(courseOptional.isPresent());
            Course updatedCourse = courseOptional.get();
            Assertions.assertEquals(newName, updatedCourse.getName());
        });
    }

    @Test
    @DisplayName("Test admin can delete course")
    public void testAdminDeleteCourse() {
        assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create course
            Course course = new Course();
            course.setName(TestUtil.generateRandomString());
            Course savedCourse = courseRepository.save(course);

            // Delete course
            mockMvc.perform(delete("/courses/" + savedCourse.getId())
                            .with(user(savedAdmin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andReturn();

            // Check course is deleted
            Optional<Course> courseOptional = courseRepository.findById(savedCourse.getId());
            Assertions.assertFalse(courseOptional.isPresent());
        });
    }

    // AI-generated-content
    // tool: ChatGPT
    // version: latest
    // usage: generate based on testAdminCreateUserFromCSVFile and Course batch endpoint
    @Test
    public void testAdminCreateCourseFromCSVFile() {
        assertDoesNotThrow(() -> {
            // Create and save an admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test courses
            Course course1 = new Course();
            course1.setName(TestUtil.generateRandomString(10));
            Course course2 = new Course();
            course2.setName(TestUtil.generateRandomString(10));

            // Prepare CSV content using course data
            String csvContent = String.format(
                    "name\n" +
                            "%s\n" +
                            "%s\n",
                    course1.getName(),
                    course2.getName()
            );

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "courses.csv",
                    MediaType.TEXT_PLAIN_VALUE,
                    csvContent.getBytes()
            );

            // Perform batch upload
            mockMvc.perform(multipart("/courses/batch")
                            .file(file)
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").isNumber())
                    .andExpect(jsonPath("$.data[1].id").isNumber())
                    .andReturn();

            // Verify courses in database
            assertThat(courseRepository.findByName(course1.getName())).isPresent();
            assertThat(courseRepository.findByName(course2.getName())).isPresent();
            assertThat(courseRepository.findByName(course1.getName()).get().getName()).isEqualTo(course1.getName());
            assertThat(courseRepository.findByName(course2.getName()).get().getName()).isEqualTo(course2.getName());
        });
    }
}