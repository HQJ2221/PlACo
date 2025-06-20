package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.*;
import com.example.restservice.repository.*;
import com.example.restservice.service.FileService;
import com.example.restservice.service.JudgeService;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.BufferedWriter;
import java.io.FileWriter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class SubmissionControllerTest {
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

    @Autowired
    public SubmissionRepository submissionRepository;

    @Autowired
    public SubmissionFileRepository submissionFileRepository;

    @Autowired
    public FileRepository fileRepository;

    @Autowired
    public JudgeService judgeService;

    @Autowired
    public FileService fileService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("Test admin can get judges for submission")
    public void testGetJudges() {
        Assumptions.assumeThat(judgeService.getJudge0BaseUrl()).isNotBlank();
        Assumptions.assumeThat(judgeService.getJudge0AuthHeader()).isNotBlank();
        Assumptions.assumeThat(judgeService.getJudge0AuthToken()).isNotBlank();
        Assertions.assertDoesNotThrow(() -> {
            // Create admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            java.io.File tempFile = java.io.File.createTempFile("echo", ".py", new java.io.File(fileService.getUploadDir()));
            tempFile.deleteOnExit();
            String pythonCode = "import sys\n" +
                    "input_data = sys.stdin.read()\n" +
                    "print(input_data)\n";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write(pythonCode);
            }
            // create assignment
            Assignment assignment = new Assignment();
            assignmentRepository.save(assignment);
            // create test case
            TestCase testCase1 = new TestCase();
            testCase1.setAssignment(assignment);
            testCase1.setStdin("dGVzdA==");
            testCase1.setExpectedOutput("dGVzdA==");
            testCaseRepository.save(testCase1);
            // create test case
            TestCase testCase2 = new TestCase();
            testCase2.setAssignment(assignment);
            testCase2.setStdin("aGVsbG8=");
            testCase2.setExpectedOutput("aGVsbG8=");
            testCaseRepository.save(testCase2);
            // create submission
            Submission submission = new Submission();
            submission.setAssignment(assignment);
            submission.setProgrammingLanguage(ProgrammingLanguage.PYTHON_3_11_2);
            submissionRepository.save(submission);
            // create file
            File file = new File();
            file.setOriginalName(tempFile.getName());
            file.setFileName(tempFile.getName());
            fileRepository.save(file);
            // create submissionfile
            SubmissionFile submissionFile = new SubmissionFile();
            submissionFile.setSubmission(submission);
            submissionFile.setFile(file);
            submissionFileRepository.save(submissionFile);
            judgeService.batchJudge(submission.getId());
            // get judges
            Thread.sleep(5000);
            mockMvc.perform(get("/submissions/{id}/judges", submission.getId())
                            .with(user(savedAdmin)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].status.description").value("Accepted"))
                    .andExpect(jsonPath("$.data[1].status.description").value("Accepted"))
                    .andReturn();
        });
    }
}