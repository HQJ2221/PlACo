package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.File;
import com.example.restservice.model.Role;
import com.example.restservice.model.User;
import com.example.restservice.repository.FileRepository;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.service.FileService;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// AI-generated-content
// tool: ChatGPT
// version: latest
// usage: generated based on AssignmentFileControllerTest

// TODO: delete files after test

@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class FileControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private Path uploadDir;

    @BeforeEach
    @DisplayName("Set up MockMvc with REST documentation and temporary upload directory")
    public void setUp(RestDocumentationContextProvider restDocumentation) throws IOException {
        // Set up temporary upload directory
        uploadDir = Path.of(fileService.getUploadDir());
        System.setProperty("file.upload-dir", fileService.getUploadDir());
        Files.createDirectories(uploadDir);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()) // Added Spring Security configuration
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @AfterEach
    @DisplayName("Clean up temporary files and reset system properties")
    public void tearDown() {
        fileRepository.deleteAll();
        System.clearProperty("file.upload-dir");
    }

    private File createTempFileWithMetadata(String filename, String content) throws IOException {
        // Create temporary file in upload directory
        java.io.File tempFile = java.io.File.createTempFile("test_", filename, uploadDir.toFile());
        tempFile.deleteOnExit();

        // Write content to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(content);
        }

        // Create and save metadata
        File file = new File();
        file.setOriginalName(tempFile.getName());
        file.setFileName(tempFile.getName());
        return fileRepository.save(file);
    }

//    @Test
//    @DisplayName("Test successful file upload")
//    public void testCreateFile() throws Exception {
//        // Create regular user
//        User user = TestUtil.generateRandomUser(Role.USER);
//        User savedUser = userRepository.save(user);
//
//        String content = "Hello, World!";
//        MockMultipartFile multipartFile = new MockMultipartFile(
//                "file",
//                "test.txt",
//                MediaType.TEXT_PLAIN_VALUE,
//                content.getBytes()
//        );
//
//        MvcResult result = mockMvc.perform(multipart("/files")
//                        .file(multipartFile)
//                        .with(user(savedUser)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.data.id").isNumber())
//                .andReturn();
//
//        // Parse response to get file ID
//        String responseContent = result.getResponse().getContentAsString();
//        JSONObject jsonResponse = new JSONObject(responseContent);
//        Long fileId = jsonResponse.getJSONObject("data").getLong("id");
//
//        // Verify file metadata in repository
//        File savedFile = fileRepository.findById(fileId).orElseThrow();
//        Assertions.assertEquals("test.txt", savedFile.getOriginalName());
//
//        // Verify physical file exists in temporary directory
//        Path filePath = uploadDir.resolve(savedFile.getFileName());
//        Assertions.assertTrue(Files.exists(filePath));
//        Assertions.assertEquals(content, Files.readString(filePath));
//    }

    @Test
    @DisplayName("Test file upload with empty file")
    public void testCreateEmptyFile() throws Exception {
        // Create regular user
        User user = TestUtil.generateRandomUser(Role.USER);
        User savedUser = userRepository.save(user);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/files")
                        .file(multipartFile)
                        .with(user(savedUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("上传文件不能为空"));
    }

    @Test
    @DisplayName("Test retrieval of all files metadata")
    public void testGetAllFilesMetadata() throws Exception {
        // Create admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        // Create test file with physical file and metadata
        String content = "Hello, World!";
        File savedFile = createTempFileWithMetadata(".txt", content);

        mockMvc.perform(get("/files")
                        .with(user(savedAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].originalName").isString());
    }

    @Test
    @DisplayName("Test download all files as ZIP")
    public void testGetAllFilesAsZip() throws Exception {
        // Create admin user
        User admin = TestUtil.generateRandomUser(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);

        // Create test file with physical file and metadata
        String content = "Test content";
        File savedFile = createTempFileWithMetadata(".txt", content);

        mockMvc.perform(get("/files")
                        .param("download", "true")
                        .with(user(savedAdmin)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"files.zip\""));
    }

    @Test
    @DisplayName("Test retrieval of file metadata by ID")
    public void testGetFileMetadataById() throws Exception {
        // Create regular user
        User user = TestUtil.generateRandomUser(Role.USER);
        User savedUser = userRepository.save(user);

        // Create test file with physical file and metadata
        String content = "Hello, World!";
        File savedFile = createTempFileWithMetadata(".txt", content);

        mockMvc.perform(get("/files/{id}", savedFile.getId())
                        .param("metadata", "true")
                        .with(user(savedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(savedFile.getId()))
                .andExpect(jsonPath("$.data.originalName").value(savedFile.getOriginalName()));
    }

//    @Test
//    @DisplayName("Test download file by ID")
//    public void testGetFileById() throws Exception {
//        // Create regular user
//        User user = TestUtil.generateRandomUser(Role.USER);
//        User savedUser = userRepository.save(user);
//
//        // Create test file with physical file and metadata
//        String content = "Test content";
//        File savedFile = createTempFileWithMetadata(".txt", content);
//
//        mockMvc.perform(get("/files/{id}", savedFile.getId())
//                        .with(user(savedUser)))
//                .andExpect(status().isOk())
//                .andExpect(header().string("Content-Type", "text/plain"))
//                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + savedFile.getOriginalName() + "\""));
//    }

    @Test
    @DisplayName("Test retrieval of non-existent file")
    public void testGetNonExistentFile() throws Exception {
        // Create regular user
        User user = TestUtil.generateRandomUser(Role.USER);
        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/files/{id}", 0L)
                        .with(user(savedUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("文件未找到"));
    }

//    @Test
//    @DisplayName("Test deletion of file")
//    public void testDeleteFile() throws Exception {
//        // Create regular user
//        User user = TestUtil.generateRandomUser(Role.USER);
//        User savedUser = userRepository.save(user);
//
//        // Create test file with physical file and metadata
//        String content = "Test content";
//        File savedFile = createTempFileWithMetadata(".txt", content);
//
//        // Verify physical file exists before deletion
//        Path filePath = uploadDir.resolve(savedFile.getFileName());
//        Assertions.assertTrue(Files.exists(filePath));
//
//        mockMvc.perform(delete("/files/{id}", savedFile.getId())
//                        .with(user(savedUser)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"));
//
//        // Verify file is deleted
//        Assertions.assertFalse(Files.exists(filePath));
//        Assertions.assertFalse(fileRepository.existsById(savedFile.getId()));
//    }

    @Test
    @DisplayName("Test deletion of non-existent file")
    public void testDeleteNonExistentFile() throws Exception {
        // Create regular user
        User user = TestUtil.generateRandomUser(Role.USER);
        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/files/{id}", 0L)
                        .with(user(savedUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"));
    }
}