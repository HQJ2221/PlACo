package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.Course;
import com.example.restservice.model.Role;
import com.example.restservice.model.RoleInCourse;
import com.example.restservice.model.User;
import com.example.restservice.repository.CourseRepository;
import com.example.restservice.repository.CourseUserRepository;
import com.example.restservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class CourseUserControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseUserRepository courseUserRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()) // Added Spring Security configuration
                .build();
    }

    // AI-generated-content
    // tool: ChatGPT
    // version: latest
    // usage: generate based on testAdminCreateUserFromCSVFile and CourseUser batch endpoint
    @Test
    public void testAdminCreateCourseUserFromCSVFile() {
        assertDoesNotThrow(() -> {
            // Create and save an admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create and save test users
            User user1 = TestUtil.generateRandomUser(Role.USER);
            User user2 = TestUtil.generateRandomUser(Role.USER);
            userRepository.save(user1);
            userRepository.save(user2);

            // Create and save test courses
            Course course1 = new Course();
            course1.setName(TestUtil.generateRandomString(10));
            Course course2 = new Course();
            course2.setName(TestUtil.generateRandomString(10));
            courseRepository.save(course1);
            courseRepository.save(course2);

            // Prepare CSV content with course-user relationships
            String csvContent = String.format(
                    "username,course_name,role\n" +
                            "%s,%s,%s\n" +
                            "%s,%s,%s\n",
                    user1.getUsername(), course1.getName(), RoleInCourse.INSTRUCTOR.toString(),
                    user2.getUsername(), course2.getName(), RoleInCourse.STUDENT.toString()
            );

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "courseusers.csv",
                    MediaType.TEXT_PLAIN_VALUE,
                    csvContent.getBytes()
            );

            // Perform batch upload
            mockMvc.perform(multipart("/course-users/batch")
                            .file(file)
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").isNumber())
                    .andExpect(jsonPath("$.data[1].id").isNumber())
                    .andReturn();

            // Verify course-user relationships in database
            assertThat(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(course1.getId(), user1.getId()).isPresent());
            assertThat(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(course1.getId(), user1.getId()).isPresent());
        });
    }
}
