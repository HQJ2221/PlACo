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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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
public class AssignmentFileControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AssignmentFileRepository assignmentFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private FileRepository fileRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("Test successful creation of assignment file by admin")
    public void testAdminCreateAssignmentFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create assignment
            Course course = TestUtil.generateRandomCourse();
            Assignment assignment = TestUtil.generateRandomAssignment();
            courseRepository.save(course);
            assignment.setUser(savedAdmin);
            assignment.setCourse(course);
            Assignment savedAssignment = assignmentRepository.save(assignment);

            // Create file
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);

            // Create assignmentFile JSON
            JSONObject json = new JSONObject();
            JSONObject assignmentJson = new JSONObject();
            assignmentJson.put("id", savedAssignment.getId());
            JSONObject fileJson = new JSONObject();
            fileJson.put("id", savedFile.getId());
            json.put("assignment", assignmentJson);
            json.put("file", fileJson);

            mockMvc.perform(post("/assignment-files")
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
    @DisplayName("Test retrieval of all assignment files")
    public void testGetAllAssignmentFiles() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test data
            Course course = TestUtil.generateRandomCourse();
            Assignment assignment = TestUtil.generateRandomAssignment();
            courseRepository.save(course);
            assignment.setUser(savedAdmin);
            assignment.setCourse(course);
            Assignment savedAssignment = assignmentRepository.save(assignment);
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            AssignmentFile assignmentFile = new AssignmentFile();
            assignmentFile.setAssignment(savedAssignment);
            assignmentFile.setFile(savedFile);
            assignmentFileRepository.save(assignmentFile);

            // Perform GET request
            mockMvc.perform(get("/assignment-files")
                            .with(user(savedAdmin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").isNumber())
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test retrieval of assignment file by ID")
    public void testGetAssignmentFileById() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test data
            Course course = TestUtil.generateRandomCourse();
            Assignment assignment = TestUtil.generateRandomAssignment();
            courseRepository.save(course);
            assignment.setUser(savedAdmin);
            assignment.setCourse(course);
            Assignment savedAssignment = assignmentRepository.save(assignment);
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            AssignmentFile assignmentFile = new AssignmentFile();
            assignmentFile.setAssignment(savedAssignment);
            assignmentFile.setFile(savedFile);
            AssignmentFile savedAssignmentFile = assignmentFileRepository.save(assignmentFile);

            // Perform GET request
            mockMvc.perform(get("/assignment-files/{id}", savedAssignmentFile.getId())
                            .with(user(savedAdmin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").value(savedAssignmentFile.getId()))
                    .andExpect(jsonPath("$.data.assignment.id").value(savedAssignment.getId()))
                    .andExpect(jsonPath("$.data.file.id").value(savedFile.getId()))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test retrieval of assignment files by assignment ID")
    public void testGetAssignmentFilesByAssignmentId() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test data
            Course course = TestUtil.generateRandomCourse();
            Assignment assignment = TestUtil.generateRandomAssignment();
            courseRepository.save(course);
            assignment.setUser(savedAdmin);
            assignment.setCourse(course);
            Assignment savedAssignment = assignmentRepository.save(assignment);
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            AssignmentFile assignmentFile = new AssignmentFile();
            assignmentFile.setAssignment(savedAssignment);
            assignmentFile.setFile(savedFile);
            assignmentFileRepository.save(assignmentFile);

            // Perform GET request
            mockMvc.perform(get("/assignment-files")
                            .param("assignment-id", savedAssignment.getId().toString())
                            .with(user(savedAdmin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].assignment.id").value(savedAssignment.getId()))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test deletion of assignment file")
    public void testDeleteAssignmentFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test data
            Course course = TestUtil.generateRandomCourse();
            Assignment assignment = TestUtil.generateRandomAssignment();
            courseRepository.save(course);
            assignment.setUser(savedAdmin);
            assignment.setCourse(course);
            Assignment savedAssignment = assignmentRepository.save(assignment);
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            AssignmentFile assignmentFile = new AssignmentFile();
            assignmentFile.setAssignment(savedAssignment);
            assignmentFile.setFile(savedFile);
            AssignmentFile savedAssignmentFile = assignmentFileRepository.save(assignmentFile);

            // Perform DELETE request
            mockMvc.perform(delete("/assignment-files/{id}", savedAssignmentFile.getId())
                            .with(user(savedAdmin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andReturn();

            // Verify deletion
            Assertions.assertFalse(assignmentFileRepository.findById(savedAssignmentFile.getId()).isPresent());
        });
    }

    @Test
    @DisplayName("Test creation of assignment file with non-existent assignment")
    public void testCreateAssignmentFileWithNonExistentAssignment() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create file
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);

            // Create assignmentFile JSON with non-existent assignment
            JSONObject json = new JSONObject();
            JSONObject assignmentJson = new JSONObject();
            assignmentJson.put("id", 0L); // Non-existent assignment ID
            JSONObject fileJson = new JSONObject();
            fileJson.put("id", savedFile.getId());
            json.put("assignment", assignmentJson);
            json.put("file", fileJson);

            mockMvc.perform(post("/assignment-files")
                            .with(user(savedAdmin))
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("Assignment or File does not exist"))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test creation of duplicate assignment file")
    public void testCreateDuplicateAssignmentFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create assignment
            Course course = TestUtil.generateRandomCourse();
            Assignment assignment = TestUtil.generateRandomAssignment();
            courseRepository.save(course);
            assignment.setUser(savedAdmin);
            assignment.setCourse(course);
            Assignment savedAssignment = assignmentRepository.save(assignment);

            // Create file
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);

            // Create initial assignmentFile
            AssignmentFile assignmentFile = new AssignmentFile();
            assignmentFile.setAssignment(savedAssignment);
            assignmentFile.setFile(savedFile);
            assignmentFileRepository.save(assignmentFile);

            // Create duplicate assignmentFile JSON
            JSONObject json = new JSONObject();
            JSONObject assignmentJson = new JSONObject();
            assignmentJson.put("id", savedAssignment.getId());
            JSONObject fileJson = new JSONObject();
            fileJson.put("id", savedFile.getId());
            json.put("assignment", assignmentJson);
            json.put("file", fileJson);

            mockMvc.perform(post("/assignment-files")
                            .with(user(savedAdmin))
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("AssignmentFile already exists"))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test retrieval of non-existent assignment file")
    public void testGetNonExistentAssignmentFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            mockMvc.perform(get("/assignment-files/{id}", 0L)
                            .with(user(savedAdmin)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("AssignmentFile not found"))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test deletion of non-existent assignment file")
    public void testDeleteNonExistentAssignmentFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            mockMvc.perform(delete("/assignment-files/{id}", 0L)
                            .with(user(savedAdmin)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("AssignmentFile not found"))
                    .andReturn();
        });
    }
}