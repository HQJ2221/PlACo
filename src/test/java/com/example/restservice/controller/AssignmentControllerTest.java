package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.*;
import com.example.restservice.repository.*;
import net.minidev.json.JSONObject;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class AssignmentControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private CourseUserRepository courseUserRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("Admin Can Get Assignment By ID")
    public void testGetAssignmentById() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        Course course = TestUtil.generateRandomCourse();
        courseRepository.save(course);

        Assignment assignment = TestUtil.generateRandomAssignment();
        assignment.setUser(savedAdmin);
        assignment.setCourse(course);
        assignment.setDueDate(LocalDateTime.of(2025, 1, 1, 1, 1, 1));
        Assignment savedAssignment = assignmentRepository.save(assignment);

        mockMvc.perform(get("/assignments/{id}", savedAssignment.getId())
                        .with(user(savedAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(savedAssignment.getId()))
                .andExpect(jsonPath("$.data.dueDate").value(savedAssignment.getDueDate().toString()))
                .andDo(document("assignments/get",
                        pathParameters(
                                parameterWithName("id").description("The ID of the assignment")
                        ),
                        responseFields(
                                fieldWithPath("status").ignored(),
                                fieldWithPath("message").ignored(),
                                fieldWithPath("data").ignored(),
                                fieldWithPath("data.id").ignored(),
                                subsectionWithPath("data.user").type(JsonFieldType.OBJECT).description("Creator"),
                                subsectionWithPath("data.course").type(JsonFieldType.OBJECT).description("Course"),
                                fieldWithPath("data.dueDate").type(JsonFieldType.STRING).description("Due date"),
                                fieldWithPath("data.fullMark").ignored(),
                                fieldWithPath("data.publishTime").ignored(),
                                fieldWithPath("data.title").ignored(),
                                fieldWithPath("data.description").ignored(),
                                fieldWithPath("data.maxAttempts").ignored(),
                                fieldWithPath("data.type").type(JsonFieldType.STRING).description("Type (CODE or FILE)"),
                                fieldWithPath("data.createTime").ignored(),
                                fieldWithPath("data.needOCR").ignored(),
                                fieldWithPath("data.programmingLanguages").type(JsonFieldType.ARRAY).description("Programming languages allowed (Set<ProgrammingLanguage>)").optional(),
                                fieldWithPath("data.testCases").type(JsonFieldType.ARRAY).description("Test cases (List<TestCase>)").optional(),
                                fieldWithPath("metadata").ignored()
                        )
                ));
    }

    @Test
    @DisplayName("Admin Can Update Assignment")
    public void testAdminUpdateAssignment() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        Assignment assignment = TestUtil.generateRandomAssignment();
        Assignment savedAssignment = assignmentRepository.save(assignment);
        JSONObject json = new JSONObject();
        json.put("dueDate", "2025-01-01T00:00:00.000Z");

        mockMvc.perform(put("/assignments/{id}", savedAssignment.getId())
                        .with(user(savedAdmin))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.dueDate").value("2025-01-01T00:00:00"))
                .andDo(document("assignments/put"));
    }

    @Test
    @DisplayName("Student in Course Can Retrieve All Assignments for That Course")
    public void testStudentGetAssignmentsByCourseId() {
        Assertions.assertDoesNotThrow(() -> {
            // Create user, course, and assignment
            User user = TestUtil.generateRandomUser(Role.USER);
            User savedUser = userRepository.save(user);

            Course course = TestUtil.generateRandomCourse();
            Course savedCourse = courseRepository.save(course);

            CourseUser courseUser = new CourseUser();
            courseUser.setRoleInCourse(RoleInCourse.STUDENT);
            courseUser.setUser(savedUser);
            courseUser.setCourse(savedCourse);
            courseUserRepository.save(courseUser);

            Assignment assignment = TestUtil.generateRandomAssignment();
            assignment.setUser(savedUser);
            assignment.setCourse(savedCourse);
            Assignment savedAssignment = assignmentRepository.save(assignment);

            mockMvc.perform(get("/assignments?course-id={id}", savedCourse.getId())
                            .with(user(savedUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(savedAssignment.getId()))
                    .andExpect(jsonPath("$.data[0].title").value(savedAssignment.getTitle()))
                    .andExpect(jsonPath("$.data[0].description").value(savedAssignment.getDescription()))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Course Instructor Can Retrieve All Assignments for That Course")
    public void testInstructorGetAssignmentsByCourseId() {
        Assertions.assertDoesNotThrow(() -> {
            // Create user, course, and assignment
            User user = TestUtil.generateRandomUser(Role.USER);
            User savedUser = userRepository.save(user);

            Course course = TestUtil.generateRandomCourse();
            Course savedCourse = courseRepository.save(course);

            CourseUser courseUser = new CourseUser();
            courseUser.setRoleInCourse(RoleInCourse.INSTRUCTOR);
            courseUser.setUser(savedUser);
            courseUser.setCourse(savedCourse);
            courseUserRepository.save(courseUser);

            Assignment assignment = TestUtil.generateRandomAssignment();
            assignment.setUser(savedUser);
            assignment.setCourse(savedCourse);
            Assignment savedAssignment = assignmentRepository.save(assignment);

            mockMvc.perform(get("/assignments?course-id={id}", savedCourse.getId())
                            .with(user(savedUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(savedAssignment.getId()))
                    .andExpect(jsonPath("$.data[0].title").value(savedAssignment.getTitle()))
                    .andExpect(jsonPath("$.data[0].description").value(savedAssignment.getDescription()))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Non-Course User Cannot Retrieve Assignments for Course")
    public void testNonCourseUserGetAssignmentsByCourseId() {
        Assertions.assertDoesNotThrow(() -> {
            // Create non-course user
            User nonCourseUser = TestUtil.generateRandomUser(Role.USER);
            User savedNonCourseUser = userRepository.save(nonCourseUser);

            // Create user, course, and assignment
            User user = TestUtil.generateRandomUser(Role.USER);
            User savedUser = userRepository.save(user);

            Course course = TestUtil.generateRandomCourse();
            Course savedCourse = courseRepository.save(course);

            Assignment assignment = TestUtil.generateRandomAssignment();
            assignment.setUser(savedUser);
            assignment.setCourse(savedCourse);
            assignmentRepository.save(assignment);

            mockMvc.perform(get("/assignments?course-id={id}", savedCourse.getId())
                            .with(user(savedNonCourseUser)))
                    .andExpect(status().isForbidden())
                    .andReturn();
        });
    }
}