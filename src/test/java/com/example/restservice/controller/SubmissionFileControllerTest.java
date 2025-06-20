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
// usage: generated based on AssignmentFileControllerTest

@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class SubmissionFileControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SubmissionFileRepository submissionFileRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    private MockMvc mockMvc;

    @BeforeEach
    @DisplayName("Set up MockMvc with REST documentation configuration")
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()) // Added springSecurity to enable user() authentication
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("Test successful creation of submission file by admin")
    public void testAdminCreateSubmissionFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // create assignment
            Assignment assignment = new Assignment();
            assignmentRepository.save(assignment);
            // Create submission
            Submission submission = TestUtil.generateRandomSubmission();
            submission.setAssignment(assignment);
            Submission savedSubmission = submissionRepository.save(submission);
            // Create file
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            // Create submissionFile JSON
            JSONObject json = new JSONObject();
            JSONObject submissionJson = new JSONObject();
            submissionJson.put("id", savedSubmission.getId());
            JSONObject fileJson = new JSONObject();
            fileJson.put("id", savedFile.getId());
            json.put("submission", submissionJson);
            json.put("file", fileJson);
            mockMvc.perform(post("/submission-files")
                            .with(user(savedAdmin)) // Use saved admin user
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test retrieval of all submission files")
    public void testGetAllSubmissionFiles() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test data
            Submission submission = TestUtil.generateRandomSubmission();
            Submission savedSubmission = submissionRepository.save(submission);
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            SubmissionFile submissionFile = new SubmissionFile();
            submissionFile.setSubmission(savedSubmission);
            submissionFile.setFile(savedFile);
            submissionFileRepository.save(submissionFile);

            // Perform GET request
            mockMvc.perform(get("/submission-files")
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").isNumber())
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test retrieval of submission file by ID")
    public void testGetSubmissionFileById() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test data
            Submission submission = TestUtil.generateRandomSubmission();
            Submission savedSubmission = submissionRepository.save(submission);
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            SubmissionFile submissionFile = new SubmissionFile();
            submissionFile.setSubmission(savedSubmission);
            submissionFile.setFile(savedFile);
            SubmissionFile savedSubmissionFile = submissionFileRepository.save(submissionFile);

            // Perform GET request
            mockMvc.perform(get("/submission-files/{id}", savedSubmissionFile.getId())
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").value(savedSubmissionFile.getId()))
                    .andExpect(jsonPath("$.data.submission.id").value(savedSubmission.getId()))
                    .andExpect(jsonPath("$.data.file.id").value(savedFile.getId()))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test retrieval of submission files by submission ID")
    public void testGetSubmissionFilesBySubmissionId() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test data
            Submission submission = TestUtil.generateRandomSubmission();
            Submission savedSubmission = submissionRepository.save(submission);
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            SubmissionFile submissionFile = new SubmissionFile();
            submissionFile.setSubmission(savedSubmission);
            submissionFile.setFile(savedFile);
            submissionFileRepository.save(submissionFile);

            // Perform GET request
            mockMvc.perform(get("/submission-files")
                            .with(user(savedAdmin)) // Use saved admin user
                            .param("submission-id", savedSubmission.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].submission.id").value(savedSubmission.getId()))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test deletion of submission file")
    public void testDeleteSubmissionFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create test data
            Submission submission = TestUtil.generateRandomSubmission();
            Submission savedSubmission = submissionRepository.save(submission);
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            SubmissionFile submissionFile = new SubmissionFile();
            submissionFile.setSubmission(savedSubmission);
            submissionFile.setFile(savedFile);
            SubmissionFile savedSubmissionFile = submissionFileRepository.save(submissionFile);

            // Perform DELETE request
            mockMvc.perform(delete("/submission-files/{id}", savedSubmissionFile.getId())
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andReturn();

            // Verify deletion
            Assertions.assertFalse(submissionFileRepository.findById(savedSubmissionFile.getId()).isPresent());
        });
    }

    @Test
    @DisplayName("Test creation of submission file with non-existent submission")
    public void testCreateSubmissionFileWithNonExistentSubmission() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create file
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            // Create submissionFile JSON with non-existent submission
            JSONObject json = new JSONObject();
            JSONObject submissionJson = new JSONObject();
            submissionJson.put("id", 999L); // Non-existent submission ID
            JSONObject fileJson = new JSONObject();
            fileJson.put("id", savedFile.getId());
            json.put("submission", submissionJson);
            json.put("file", fileJson);
            mockMvc.perform(post("/submission-files")
                            .with(user(savedAdmin)) // Use saved admin user
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("Submission or File does not exist"))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test creation of duplicate submission file")
    public void testCreateDuplicateSubmissionFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create submission
            Submission submission = TestUtil.generateRandomSubmission();
            Submission savedSubmission = submissionRepository.save(submission);
            // Create file
            File file = TestUtil.generateRandomFile();
            File savedFile = fileRepository.save(file);
            // Create initial submissionFile
            SubmissionFile submissionFile = new SubmissionFile();
            submissionFile.setSubmission(savedSubmission);
            submissionFile.setFile(savedFile);
            submissionFileRepository.save(submissionFile);
            // Create duplicate submissionFile JSON
            JSONObject json = new JSONObject();
            JSONObject submissionJson = new JSONObject();
            submissionJson.put("id", savedSubmission.getId());
            JSONObject fileJson = new JSONObject();
            fileJson.put("id", savedFile.getId());
            json.put("submission", submissionJson);
            json.put("file", fileJson);
            mockMvc.perform(post("/submission-files")
                            .with(user(savedAdmin)) // Use saved admin user
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("SubmissionFile already exists"))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test retrieval of non-existent submission file")
    public void testGetNonExistentSubmissionFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            mockMvc.perform(get("/submission-files/{id}", 999L)
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("SubmissionFile not found"))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("Test deletion of non-existent submission file")
    public void testDeleteNonExistentSubmissionFile() {
        Assertions.assertDoesNotThrow(() -> {
            // Create and save admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            mockMvc.perform(delete("/submission-files/{id}", 999L)
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("SubmissionFile not found"))
                    .andReturn();
        });
    }
}