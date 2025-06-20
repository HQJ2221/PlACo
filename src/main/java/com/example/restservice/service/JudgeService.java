package com.example.restservice.service;

import com.example.restservice.model.Submission;
import com.example.restservice.model.SubmissionFile;
import com.example.restservice.model.TestCase;
import com.example.restservice.repository.AssignmentRepository;
import com.example.restservice.repository.SubmissionFileRepository;
import com.example.restservice.repository.SubmissionRepository;
import com.example.restservice.repository.TestCaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class JudgeService {
    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);
    private RestClient restClient;
    private FileService fileService;
    private AssignmentRepository assignmentRepository;
    private TestCaseRepository testCaseRepository;
    private SubmissionRepository submissionRepository;
    private SubmissionFileRepository submissionFileRepository;
    @Value("${judge0.base-url}")
    private String judge0BaseUrl;
    @Value("${judge0.auth-header}")
    private String judge0AuthHeader;
    @Value("${judge0.auth-token}")
    private String judge0AuthToken;
    private final String callbackUrl = null;

    public JudgeService(FileService fileService, AssignmentRepository assignmentRepository, TestCaseRepository testCaseRepository, SubmissionRepository submissionRepository, SubmissionFileRepository submissionFileRepository) {
        this.fileService = fileService;
        this.assignmentRepository = assignmentRepository;
        this.testCaseRepository = testCaseRepository;
        this.submissionRepository = submissionRepository;
        this.submissionFileRepository = submissionFileRepository;
    }

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .baseUrl(judge0BaseUrl)
                .defaultHeader(judge0AuthHeader, judge0AuthToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public List<Map<String, Object>> getBatchResults(Long submissionId) {
        try {
            Submission submissionSaved = null;
            Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);
            if (optionalSubmission.isPresent()) {
                submissionSaved = optionalSubmission.get();
            }
            List<String> judgeIds = submissionSaved.getJudgeIds();
            String tokens = String.join(",", judgeIds);
            Map<String, List<Map<String, Object>>> response = restClient.get()
                    .uri(judge0BaseUrl + "/submissions/batch?base64_encoded=true&tokens=" + tokens)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return response.get("submissions");
        } catch (Exception e) {
            LoggerFactory.getLogger(JudgeService.class).error("Error getting batch results {}", e.toString());
        }
        return null;
    }

    public List<String> batchJudge(Long submissionId) {
        try {
            if (judge0BaseUrl.isBlank() || judge0AuthHeader.isBlank() || judge0AuthToken.isBlank()) {
                return null;
            }
            Submission submissionSaved = null;
            Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);
            if (optionalSubmission.isPresent()) {
                submissionSaved = optionalSubmission.get();
            }
            Map<String, Object> payload = prepareBatchJudgePayload(submissionSaved);
            if (payload == null) {
                return null;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(payload);

            List<Map<String, Object>> response = restClient.post()
                    .uri(judge0BaseUrl + "/submissions/batch?base64_encoded=true")
                    .contentType(APPLICATION_JSON)
                    .body(jsonString)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            List<String> judgeIds = submissionSaved.getJudgeIds();
            if (response != null && response.get(0).containsKey("token")) {
                judgeIds = response.stream()
                        .flatMap(map -> map.values().stream())
                        .map(Object::toString)
                        .collect(Collectors.toList());
                submissionSaved.setJudgeIds(judgeIds);
                submissionRepository.save(submissionSaved);
            } else {
                throw new RuntimeException("Failed to retrieve submission token from Judge0");
            }
            return judgeIds;
        } catch (Exception e) {
            throw new RuntimeException("Error submitting to Judge0: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> prepareBatchJudgePayload(Submission submission) {
        Map<String, Object> batchJudgePayload = new HashMap<>();
        List<Map<String, Object>> judgePayloads = new ArrayList<>();
        List<TestCase> testCases = testCaseRepository.findByAssignment_Id(submission.getAssignment().getId());
        if (testCases.isEmpty()) {
            return null;
        }
        for (TestCase testCase: testCases){
            Map<String, Object> judgePayload = prepareJudgePayload(submission, testCase);
            judgePayloads.add(judgePayload);
        }
        batchJudgePayload.put("submissions", judgePayloads);
        return batchJudgePayload;
    }

    private Map<String, Object> prepareJudgePayload(Submission submission, TestCase testCase) {
        Map<String, Object> payload = new java.util.HashMap<>();
        List<SubmissionFile> submissionFiles = submissionFileRepository.findBySubmission_Id(submission.getId());

        // Required attributes
        if (submissionFiles.size() == 1) {
            payload.put("source_code", fileService.getBase64ByFileName(submissionFiles.get(0).getFile().getFileName()));
            payload.put("language_id", submission.getProgrammingLanguage().getId());
        } else if (submissionFiles.size() > 1) {
            payload.put("additional_files", createBase64ZipFromSubmissionFiles(submissionFiles));
            payload.put("language_id", 89);
        } else {
            throw new RuntimeException("There are no files in this submission");
        }

        // Optional attributes
        if (testCase.getCompilerOptions() != null) {
            payload.put("compiler_options", testCase.getCompilerOptions());
        }
        if (testCase.getCommandLineArguments() != null) {
            payload.put("command_line_arguments", testCase.getCommandLineArguments());
        }
        if (testCase.getStdin() != null) {
            payload.put("stdin", Base64.getEncoder().encodeToString(testCase.getStdin().getBytes(StandardCharsets.UTF_8)));
        }
        if (testCase.getExpectedOutput() != null) {
            payload.put("expected_output", Base64.getEncoder().encodeToString(testCase.getExpectedOutput().getBytes(StandardCharsets.UTF_8)));
        }

        // Configuration
        payload.put("cpu_time_limit", testCase.getCpuTimeLimit() != null ? testCase.getCpuTimeLimit() : 2.0); // Default: 2s
        payload.put("cpu_extra_time", testCase.getCpuExtraTime() != null ? testCase.getCpuExtraTime() : 0.5); // Default: 0.5s
        payload.put("wall_time_limit", testCase.getWallTimeLimit() != null ? testCase.getWallTimeLimit() : 5.0); // Default: 5s
        payload.put("memory_limit", testCase.getMemoryLimit() != null ? testCase.getMemoryLimit() : 128000); // Default: 128MB
        payload.put("stack_limit", testCase.getStackLimit() != null ? testCase.getStackLimit() : 64000); // Default: 64MB
        payload.put("max_processes_and_or_threads", testCase.getMaxProcessesAndOrThreads() != null ? testCase.getMaxProcessesAndOrThreads() : 60);
        payload.put("enable_per_process_and_thread_time_limit", testCase.getEnablePerProcessAndThreadTimeLimit() != null ? testCase.getEnablePerProcessAndThreadTimeLimit() : false);
        payload.put("enable_per_process_and_thread_memory_limit", testCase.getEnablePerProcessAndThreadMemoryLimit() != null ? testCase.getEnablePerProcessAndThreadMemoryLimit() : false);
        payload.put("max_file_size", testCase.getMaxFileSize() != null ? testCase.getMaxFileSize() : 1024); // Default: 1MB
        payload.put("redirect_stderr_to_stdout", testCase.getRedirectStderrToStdout() != null ? testCase.getRedirectStderrToStdout() : false);
        payload.put("enable_network", testCase.getEnableNetwork() != null ? testCase.getEnableNetwork() : false);
        payload.put("number_of_runs", testCase.getNumberOfRuns() != null ? testCase.getNumberOfRuns() : 1);

        // Callback URL
        if (callbackUrl != null) {
            payload.put("callback_url", callbackUrl);
        }
        return payload;
    }

    private String createBase64ZipFromSubmissionFiles(List<SubmissionFile> submissionFiles){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (SubmissionFile submissionFile : submissionFiles) {
                ZipEntry entry = new ZipEntry(submissionFile.getFile().getOriginalName());
                zos.putNextEntry(entry);
                zos.write(fileService.getBytesArrayByFileName(submissionFile.getFile().getFileName()));
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public String getJudge0BaseUrl() {
        return judge0BaseUrl;
    }

    public void setJudge0BaseUrl(String judge0BaseUrl) {
        this.judge0BaseUrl = judge0BaseUrl;
    }

    public String getJudge0AuthHeader() {
        return judge0AuthHeader;
    }

    public void setJudge0AuthHeader(String judge0AuthHeader) {
        this.judge0AuthHeader = judge0AuthHeader;
    }

    public String getJudge0AuthToken() {
        return judge0AuthToken;
    }

    public void setJudge0AuthToken(String judge0AuthToken) {
        this.judge0AuthToken = judge0AuthToken;
    }
}
