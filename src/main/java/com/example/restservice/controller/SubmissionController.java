package com.example.restservice.controller;

import com.example.restservice.model.*;
import com.example.restservice.repository.SubmissionRepository;
import com.example.restservice.repository.AssignmentRepository;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.service.JudgeService;
import com.example.restservice.service.LogService;
import com.example.restservice.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/submissions")
public class SubmissionController {
    private final SubmissionRepository submissionRepository;

    private final AssignmentRepository assignmentRepository;

    private final UserRepository userRepository;

    private final JudgeService judgeService;

    private final LogService logService;

    private final MailService mailService;

    public SubmissionController(SubmissionRepository submissionRepository, AssignmentRepository assignmentRepository, UserRepository userRepository, JudgeService judgeService, LogService logService, MailService mailService) {
        this.submissionRepository=submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.judgeService = judgeService;
        this.logService = logService;
        this.mailService = mailService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.canCreateSubmission(authentication, #submission)")
    public ResponseEntity<?> createSubmission(@RequestBody Submission submission) {
        submission.setId(null);
        User user = userRepository.getReferenceById(submission.getUser().getId());
        Assignment assignment = assignmentRepository.getReferenceById(submission.getAssignment().getId());
        submission.setUser(user);
        submission.setAssignment(assignment);
        submissionRepository.save(submission);
        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(submission.getId());

        logService.createLog("Submission created: "+submission.getId());

        mailService.sendMail(user.getId(), "Submission Created", "Your submission has been created with id: " + submission.getId());
        return ApiResponse.ok(postReturnData);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllSubmissions() {
        List<Submission> submissions = submissionRepository.findAll();

        logService.createLog("Get all submissions");
        return ApiResponse.ok(submissions);
    }

    @GetMapping(params= {"assignment-id"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isTeacherByAssignment(authentication, #assignmentId)")
    public ResponseEntity<?> getSubmissionsByAssignmentId(@RequestParam(name = "assignment-id") Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignment_Id(assignmentId);

        logService.createLog("Get submissions by assignment: "+assignmentId);
        return ApiResponse.ok(submissions);
    }

    @GetMapping(params = {"assignment-id", "latest"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isTeacherByAssignment(authentication, #assignmentId)")
    public ResponseEntity<?> getLatestSubmission(@RequestParam(name = "assignment-id") Long assignmentId) {
        // 获取作业的所有提交
        List<Submission> submissions = submissionRepository.findByAssignment_Id(assignmentId);

        // 按用户分组，获取每个用户的最新提交
        Map<Long, Submission> latestSubmissions = new HashMap<>();
        for (Submission submission : submissions) {
            Long userId = submission.getUser().getId();
            Submission existing = latestSubmissions.get(userId);
            if (existing == null || submission.getSubmitTime().isAfter(existing.getSubmitTime())) {
                latestSubmissions.put(userId, submission);
            }
        }

        // 转换为列表
        List<Submission> result = new ArrayList<>(latestSubmissions.values());

        logService.createLog("Get latest submissions of assignment: "+assignmentId);
        return ApiResponse.ok(result);
    }

    @GetMapping(params = {"user-id", "assignment-id"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.id or @mySecurityService.isTeacherByAssignment(authentication, #assignmentId)")
    public ResponseEntity<?> getSubmissionsByUserIdAndAssignmentID(@RequestParam(name = "user-id") Long userId, @RequestParam(name = "assignment-id") Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByUser_IdAndAssignment_Id(userId, assignmentId);

        logService.createLog("Get submissions by user id :" + userId + "and assignment id :" + assignmentId);
        return ApiResponse.ok(submissions);
    }

    @GetMapping(params = {"user-id", "assignment-id", "sort", "order"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.id or @mySecurityService.isTeacherByAssignment(authentication, #assignmentId)")
    public ResponseEntity<?> getLatestSubmissionsByUserIdAndAssignmentID(@RequestParam(name = "user-id") Long userId, @RequestParam(name = "assignment-id") Long assignmentId, @RequestParam(name = "sort") String sort, @RequestParam(name = "order") String order) {
        if (sort.equals("submit-time") && order.equals("desc")){
            List<Submission> submissions = submissionRepository.findByUser_IdAndAssignment_Id(userId, assignmentId);
            //找到最晚的那一个
            if(submissions.isEmpty()){
                return ApiResponse.ok(null);
            }
            Submission latestSubmission=submissions.get(0);
            for (Submission submission : submissions){
                if(submission.getSubmitTime().isAfter(latestSubmission.getSubmitTime())){
                    latestSubmission=submission;
                }
            }

            logService.createLog("Get latest submission by user id: "+ userId + "and assignment id: "+assignmentId);
            return ApiResponse.ok(latestSubmission);
        }else{
            return ApiResponse.internalServerError();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isSubmissionOwner(authentication, #id) or @mySecurityService.isTeacherBySubmission(authentication, #id)")
    public ResponseEntity<?> getSubmissionById(@PathVariable Long id) {
        Optional<Submission> submission = submissionRepository.findById(id);

        logService.createLog("Get submission by Id: "+ id);
        return submission.map(ApiResponse::ok).orElseGet(ApiResponse::internalServerError);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isTeacherBySubmission(authentication, #id)")
    public ResponseEntity<?> updateSubmission(@PathVariable Long id, @RequestBody Submission submissionDetails) {
        Optional<Submission> optionalSubmission = submissionRepository.findById(id);
        if (optionalSubmission.isEmpty()) {
            return ApiResponse.internalServerError();
        }
        Submission submission = optionalSubmission.get();
        submission.updateWithoutId(submissionDetails);
        Submission updatedSubmission = submissionRepository.save(submission);

        logService.createLog("Update Submission: "+id);

        if (submissionDetails.getScoreVisible()){
            mailService.sendMail(submission.getUser().getId(), "Submission Score Published",
                    "Assignment: " + submission.getAssignment().getTitle() + "\n"
                            + "Course: " + submission.getAssignment().getCourse().getName());
        }
        return ApiResponse.ok(updatedSubmission);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isSubmissionOwner(authentication, #id)")
    public ResponseEntity<?> deleteSubmission(@PathVariable Long id) {
        if (!submissionRepository.existsById(id)) {
            return ApiResponse.internalServerError();
        }
        submissionRepository.deleteById(id);

        logService.createLog("Delete Submission: "+id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/judges")
    public ResponseEntity<?> getSubmissionJudges(@PathVariable Long id) {
        if (!submissionRepository.existsById(id)) {
            return ApiResponse.internalServerError();
        }
        List<Map<String, Object>> batchResults = judgeService.getBatchResults(id);

        logService.createLog("Get submission judges: "+ id);
        return ApiResponse.ok(batchResults);
    }

    @PostMapping("/{id}/judges")
    public ResponseEntity<?> createSubmissionJudges(@PathVariable Long id) {
        Optional<Submission> optionalSubmission = submissionRepository.findById(id);
        if (optionalSubmission.isEmpty()) {
            return ApiResponse.internalServerError();
        }
        Submission submission = optionalSubmission.get();
        // start judge if assignment type is CODE
        if (submission.getAssignment().getType() == AssignmentType.CODE) {
            judgeService.batchJudge(id);
        }

        logService.createLog("Create submission judges: "+ id);
        return ApiResponse.ok();
    }
}

