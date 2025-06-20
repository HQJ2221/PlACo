package com.example.restservice.controller;

import com.example.restservice.model.ApiResponse;
import com.example.restservice.model.Assignment;
import com.example.restservice.model.PostReturnData;
import com.example.restservice.model.TestCase;
import com.example.restservice.repository.AssignmentRepository;
import com.example.restservice.repository.TestCaseRepository;
import com.example.restservice.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/test-cases")
public class TestCaseController {
    private final TestCaseRepository testCaseRepository;

    private final LogService logService;
    private final AssignmentRepository assignmentRepository;

    public TestCaseController(TestCaseRepository testCaseRepository, AssignmentRepository assignmentRepository, LogService logService) {
        this.testCaseRepository = testCaseRepository;
        this.assignmentRepository = assignmentRepository;
        this.logService = logService;
    }

    @PostMapping
    public ResponseEntity<?> createTestCase(@RequestBody TestCase testCase) {
        Assignment assignment = assignmentRepository.getReferenceById(testCase.getAssignment().getId());
        testCase.setId(null);
        testCase.setAssignment(assignment);
        testCaseRepository.save(testCase);

        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(testCase.getId());

        logService.createLog("Create test cases of assignment: "+ assignment.getTitle());
        return ApiResponse.ok(postReturnData);
    }

    @GetMapping
    public ResponseEntity<?> getAllTestCases() {
        List<TestCase> testCases = testCaseRepository.findAll();

        logService.createLog("Get all testcases");
        return ApiResponse.ok(testCases);
    }

    @GetMapping(params = {"assignment-id"})
    public ResponseEntity<?> getTestCasesByAssignmentId(@RequestParam("assignment-id") Long assignmentId) {
        List<TestCase> testCases = testCaseRepository.findByAssignment_Id(assignmentId);

        logService.createLog("Get test cases by assignment id: "+ assignmentId);
        return ApiResponse.ok(testCases);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTestCaseById(@PathVariable Long id) {
        Optional<TestCase> testCaseOptional = testCaseRepository.findById(id);
        if (testCaseOptional.isEmpty()) {
            return ApiResponse.internalServerError();
        }

        logService.createLog("Get test case by id: "+ id);
        return ApiResponse.ok(testCaseOptional.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTestCase(@PathVariable Long id, @RequestBody TestCase testCaseDetails) {
        Optional<TestCase> optionalTestCase = testCaseRepository.findById(id);
        if (optionalTestCase.isEmpty()) {
            return ApiResponse.internalServerError();
        }
        TestCase testCase = optionalTestCase.get();
        testCase.setAssignment(assignmentRepository.getReferenceById(testCase.getAssignment().getId()));
        testCase.updateWithoutId(testCaseDetails);
        Assignment updatedAssignment = assignmentRepository.save(testCase.getAssignment());

        logService.createLog("Update test case by id: "+ id);
        return ApiResponse.ok(updatedAssignment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTestCaseById(@PathVariable Long id) {
        Optional<TestCase> optionalTestCase = testCaseRepository.findById(id);
        if (optionalTestCase.isEmpty()) {
            return ApiResponse.internalServerError();
        }
        TestCase testCase = optionalTestCase.get();
        testCaseRepository.delete(testCase);

        logService.createLog("Delete test case by id: "+ id);
        return ApiResponse.ok();
    }
}
