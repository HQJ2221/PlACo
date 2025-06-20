package com.example.restservice.service;

import com.example.restservice.model.*;
import com.example.restservice.repository.*;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Transactional
public class JudgeServiceTest {
    @Autowired
    public JudgeService judgeService;

    @Autowired
    public SubmissionRepository submissionRepository;

    @Autowired
    public AssignmentRepository assignmentRepository;

    @Autowired
    public TestCaseRepository testCaseRepository;

    @Autowired
    public FileRepository fileRepository;

    @Autowired
    public SubmissionFileRepository submissionFileRepository;
    @Autowired
    private FileService fileService;

    // replace with existing judge ids before test
    @Test
    public void testGetBatchResults() {
        Assumptions.assumeThat(judgeService.getJudge0BaseUrl()).isNotBlank();
        Assumptions.assumeThat(judgeService.getJudge0AuthHeader()).isNotBlank();
        Assumptions.assumeThat(judgeService.getJudge0AuthToken()).isNotBlank();
        List<String> judgeIds = Arrays.asList("", "", "");
        Submission submission = new Submission();
        submission.setJudgeIds(judgeIds);
        submissionRepository.save(submission);
        List<Map<String, Object>> batchResults = judgeService.getBatchResults(submission.getId());
        Assertions.assertNotNull(batchResults);
        Assertions.assertTrue(batchResults.size() > 0);
        Assertions.assertEquals(submission.getJudgeIds().size(), batchResults.size());
    }

    @Test
    public void testBatchJudge() throws Exception {
        Assumptions.assumeThat(judgeService.getJudge0BaseUrl()).isNotBlank();
        Assumptions.assumeThat(judgeService.getJudge0AuthHeader()).isNotBlank();
        Assumptions.assumeThat(judgeService.getJudge0AuthToken()).isNotBlank();
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
        Thread.sleep(5000);
        List<Map<String, Object>> batchResults = judgeService.getBatchResults(submission.getId());
        while (true) {
            boolean allAccepted = true;
            for (Map<String, Object> result : batchResults) {
                Map status = (Map) result.get("status");
                String statusDescription = (String) status.get("description");
                if ("In queue".equals(statusDescription) || "Processing".equals(statusDescription)) {
                    allAccepted = false;
                    break;
                } else {
                    Assertions.assertEquals("Accepted", status.get("description"));
                }
            }
            if (allAccepted) {
                break;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            batchResults = judgeService.getBatchResults(submission.getId());
        }
    }
}
