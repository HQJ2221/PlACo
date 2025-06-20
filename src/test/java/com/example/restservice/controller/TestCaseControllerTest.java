package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.Assignment;
import com.example.restservice.model.Role;
import com.example.restservice.model.TestCase;
import com.example.restservice.model.User;
import com.example.restservice.repository.AssignmentRepository;
import com.example.restservice.repository.TestCaseRepository;
import com.example.restservice.repository.UserRepository;
import net.minidev.json.JSONObject;
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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class TestCaseControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("Admin Can Create Test Case")
    public void testCreateTestCase() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        // Create and save assignment
        Assignment assignment = TestUtil.generateRandomAssignment();
        assignment.setUser(savedAdmin);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Create assignment JSON
        JSONObject assignmentJson = new JSONObject();
        assignmentJson.put("id", savedAssignment.getId());
        // Create test case JSON
        JSONObject json = new JSONObject();
        json.put("assignment", assignmentJson);
        json.put("stdin", "test input");
        json.put("expectedOutput", "test output");

        mockMvc.perform(post("/test-cases")
                        .with(user(savedAdmin))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
    }

    @Test
    @DisplayName("Admin Can Get All Test Cases")
    public void testGetAllTestCases() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        // Create and save assignment
        Assignment assignment = TestUtil.generateRandomAssignment();
        assignment.setUser(savedAdmin);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Create and save test case
        TestCase testCase = new TestCase();
        testCase.setAssignment(savedAssignment);
        testCase.setStdin("test input");
        testCase.setExpectedOutput("test output");
        testCaseRepository.save(testCase);

        mockMvc.perform(get("/test-cases")
                        .with(user(savedAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testCase.getId()))
                .andReturn();
    }

    @Test
    @DisplayName("Admin Can Get Test Cases By Assignment ID")
    public void testGetTestCasesByAssignmentId() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        // Create and save assignment
        Assignment assignment = TestUtil.generateRandomAssignment();
        assignment.setUser(savedAdmin);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Create and save test case
        TestCase testCase = new TestCase();
        testCase.setAssignment(savedAssignment);
        testCase.setStdin("test input");
        testCase.setExpectedOutput("test output");
        testCaseRepository.save(testCase);

        mockMvc.perform(get("/test-cases?assignment-id={id}", savedAssignment.getId())
                        .with(user(savedAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testCase.getId()))
                .andReturn();
    }

    @Test
    @DisplayName("Admin Can Get Test Case By ID")
    public void testGetTestCaseById() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        // Create and save assignment
        Assignment assignment = TestUtil.generateRandomAssignment();
        assignment.setUser(savedAdmin);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Create and save test case
        TestCase testCase = new TestCase();
        testCase.setAssignment(savedAssignment);
        testCase.setStdin("test input");
        testCase.setExpectedOutput("test output");
        TestCase savedTestCase = testCaseRepository.save(testCase);

        mockMvc.perform(get("/test-cases/{id}", savedTestCase.getId())
                        .with(user(savedAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(savedTestCase.getId()))
                .andExpect(jsonPath("$.data.stdin").value(savedTestCase.getStdin()))
                .andExpect(jsonPath("$.data.expectedOutput").value(savedTestCase.getExpectedOutput()))
                .andReturn();
    }

    @Test
    @DisplayName("Admin Can Update Test Case")
    public void testUpdateTestCase() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        // Create and save assignment
        Assignment assignment = TestUtil.generateRandomAssignment();
        assignment.setUser(savedAdmin);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Create and save test case
        TestCase testCase = new TestCase();
        testCase.setAssignment(savedAssignment);
        testCase.setStdin("old input");
        testCase.setExpectedOutput("old output");
        TestCase savedTestCase = testCaseRepository.save(testCase);

        // Create updated test case JSON
        JSONObject json = new JSONObject();
        json.put("assignment", new JSONObject().put("id", savedAssignment.getId()));
        json.put("input", "new input");
        json.put("output", "new output");

        mockMvc.perform(put("/test-cases/{id}", savedTestCase.getId())
                        .with(user(savedAdmin))
                        .content(json.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
    }

    @Test
    @DisplayName("Admin Can Delete Test Case")
    public void testDeleteTestCaseById() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        // Create and save assignment
        Assignment assignment = TestUtil.generateRandomAssignment();
        assignment.setUser(savedAdmin);
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Create and save test case
        TestCase testCase = new TestCase();
        testCase.setAssignment(savedAssignment);
        testCase.setStdin("test input");
        testCase.setExpectedOutput("test output");
        TestCase savedTestCase = testCaseRepository.save(testCase);

        mockMvc.perform(delete("/test-cases/{id}", savedTestCase.getId())
                        .with(user(savedAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
    }

    @Test
    @DisplayName("Non-Existent Test Case Returns Error")
    public void testGetNonExistentTestCase() throws Exception {
        // Create and save admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        mockMvc.perform(get("/test-cases/{id}", 999L)
                        .with(user(savedAdmin)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andReturn();
    }
}