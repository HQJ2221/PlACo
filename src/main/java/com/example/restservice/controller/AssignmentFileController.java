package com.example.restservice.controller;

import com.example.restservice.model.ApiResponse;
import com.example.restservice.model.AssignmentFile;
import com.example.restservice.model.Assignment;
import com.example.restservice.model.File;
import com.example.restservice.repository.AssignmentFileRepository;
import com.example.restservice.repository.AssignmentRepository;
import com.example.restservice.repository.FileRepository;
import com.example.restservice.model.PostReturnData;
import com.example.restservice.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/assignment-files")
public class AssignmentFileController {

    private static final Logger log = LoggerFactory.getLogger(AssignmentFileController.class);
    private final AssignmentFileRepository assignmentFileRepository;

    private final AssignmentRepository assignmentRepository;

    private final FileRepository fileRepository;

    private final LogService logService;

    public AssignmentFileController(AssignmentFileRepository assignmentFileRepository, AssignmentRepository assignmentRepository, FileRepository fileRepository, LogService logService) {
        this.assignmentFileRepository = assignmentFileRepository;
        this.assignmentRepository = assignmentRepository;
        this.fileRepository= fileRepository;
        this.logService = logService;
    }

    // 获取所有 AssignmentFile
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllAssignmentFiles() {
        List<AssignmentFile> assignmentFiles = assignmentFileRepository.findAll();

        logService.createLog("Get All Assignment Files");

        return ApiResponse.ok(assignmentFiles);
    }

    // 根据 ID 获取单个 AssignmentFile
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.inCourseByAssignmentFile(authentication, #id)")
    public ResponseEntity<?> getAssignmentFileById(@PathVariable Long id) {
        Optional<AssignmentFile> optionalAssignmentFile = assignmentFileRepository.findById(id);

        logService.createLog("Get Assignment File by ID: "+id);

        return optionalAssignmentFile.map(ApiResponse::ok).orElseGet(() -> ApiResponse.internalServerError("AssignmentFile not found"));
    }

    // 根据 Assignment ID 获取所有相关 AssignmentFile
    @GetMapping(params = {"assignment-id"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.inCourseByAssignment(authentication, #assignmentId)")
    public ResponseEntity<?> getAssignmentFilesByAssignmentId(@RequestParam(name = "assignment-id") Long assignmentId) {
        List<AssignmentFile> assignmentFiles = assignmentFileRepository.findByAssignment_Id(assignmentId);

        logService.createLog("Get Assignment Files by Assignment ID: "+assignmentId);
        return ApiResponse.ok(assignmentFiles);
    }

    // 创建一个新的 AssignmentFile
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isAssignmentOwnerByAssignmentFile(authentication, #assignmentFile)")
    public ResponseEntity<?> createAssignmentFile(@RequestBody AssignmentFile assignmentFile) {
        if (assignmentFile.getAssignment() == null || assignmentFile.getFile() == null) {
            return ResponseEntity.badRequest().build();
        }

        // 检查 Assignment 和 File 是否存在在数据库中
        Long assignmentId = assignmentFile.getAssignment().getId();
        Long fileId = assignmentFile.getFile().getId();
        if (!assignmentRepository.existsById(assignmentId) ||
                !fileRepository.existsById(fileId)) {
            return ApiResponse.internalServerError("Assignment or File does not exist");
        }

        //检查该条记录是否已存在
        Optional<AssignmentFile> existingAssignmentFile = assignmentFileRepository.findByAssignment_IdAndFile_Id(assignmentId, fileId);

        if(existingAssignmentFile.isPresent()){
            return ApiResponse.internalServerError("AssignmentFile already exists");
        }

        // 设置 Assignment 和 File 的引用
        Assignment assignment=assignmentRepository.getReferenceById(assignmentId);
        assignmentFile.setAssignment(assignment);

        File file = fileRepository.getReferenceById(fileId);
        assignmentFile.setFile(file);

        assignmentFile.setId(null); // 确保 ID 为 null，以便创建新的记录

        AssignmentFile savedAssignmentFile = assignmentFileRepository.save(assignmentFile);

        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(savedAssignmentFile.getId());

        logService.createLog("Create Assignment File: " +savedAssignmentFile.getId() );
        return ApiResponse.ok(postReturnData);
    }

    // 删除一个 AssignmentFile
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isAssignmentFileOwner(authentication, #id)")
    public ResponseEntity<?> deleteAssignmentFile(@PathVariable Long id) {
        Optional<AssignmentFile> optionalAssignmentFile = assignmentFileRepository.findById(id);
        if (optionalAssignmentFile.isPresent()) {
            assignmentFileRepository.deleteById(id);

            logService.createLog("Delete Assignment File: "+id);
            return ApiResponse.ok();
        } else {
            return ApiResponse.internalServerError("AssignmentFile not found");
        }
    }
}