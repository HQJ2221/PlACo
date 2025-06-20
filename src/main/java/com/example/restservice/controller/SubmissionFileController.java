package com.example.restservice.controller;

import com.example.restservice.model.*;
import com.example.restservice.repository.AssignmentRepository;
import com.example.restservice.repository.SubmissionFileRepository;
import com.example.restservice.repository.SubmissionRepository;
import com.example.restservice.repository.FileRepository;
import com.example.restservice.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.restservice.service.OCRService; // 新增导入

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/submission-files")
public class SubmissionFileController {

    private final SubmissionFileRepository submissionFileRepository;

    private final SubmissionRepository submissionRepository;

    private final FileRepository fileRepository;
    private final AssignmentRepository assignmentRepository;
    private final OCRService ocrService; // 新增字段

    private final LogService logService;

    public SubmissionFileController(SubmissionFileRepository submissionFileRepository,
                                    SubmissionRepository submissionRepository,
                                    FileRepository fileRepository,
                                    AssignmentRepository assignmentRepository,
                                    OCRService ocrService,
                                    LogService logService
                                    ) {
        this.submissionFileRepository = submissionFileRepository;
        this.submissionRepository = submissionRepository;
        this.fileRepository = fileRepository;
        this.assignmentRepository = assignmentRepository;
        this.ocrService=ocrService;
        this.logService=logService;
    }

    // 获取所有 SubmissionFile
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllSubmissionFiles() {
        List<SubmissionFile> submissionFiles = submissionFileRepository.findAll();

        logService.createLog("Get all submission files");
        return ApiResponse.ok(submissionFiles);
    }

    // 根据 ID 获取单个 SubmissionFile
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isSubmissionFileOwner(authentication, #id) or @mySecurityService.isTeacherBySubmissionFile(authentication, #id)")
    public ResponseEntity<?> getSubmissionFileById(@PathVariable Long id) {
        Optional<SubmissionFile> optionalSubmissionFile = submissionFileRepository.findById(id);

        logService.createLog("Get submission file by Id: "+id);
        return optionalSubmissionFile.map(ApiResponse::ok).orElseGet(() -> ApiResponse.internalServerError("SubmissionFile not found"));
    }

    // 根据 Submission ID 获取所有相关 SubmissionFile
    @GetMapping(params = {"submission-id"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isSubmissionOwner(authentication, #submissionId) or @mySecurityService.isTeacherBySubmission(authentication, #submissionId)")
    public ResponseEntity<?> getSubmissionFilesBySubmissionId(@RequestParam(name = "submission-id") Long submissionId) {
        List<SubmissionFile> submissionFiles = submissionFileRepository.findBySubmission_Id(submissionId);

        logService.createLog("Get submission files by submission id: "+submissionId);
        return ApiResponse.ok(submissionFiles);
    }

    // 创建一个新的 SubmissionFile
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isSubmissionOwner(authentication, #submissionFile.getSubmission().getId())")
    public ResponseEntity<?> createSubmissionFile(@RequestBody SubmissionFile submissionFile) {
        if (submissionFile.getSubmission() == null || submissionFile.getFile() == null) {
            return ApiResponse.internalServerError();
        }

        // 检查 Submission 和 File 是否存在在数据库中
        Long submissionId = submissionFile.getSubmission().getId();
        Long fileId = submissionFile.getFile().getId();
        if (!submissionRepository.existsById(submissionId) ||
                !fileRepository.existsById(fileId)) {
            return ApiResponse.internalServerError("Submission or File does not exist");
        }

        //检查该条记录是否已存在
        Optional<SubmissionFile> existingSubmissionFile = submissionFileRepository.findBySubmission_IdAndFile_Id(submissionId, fileId);

        if(existingSubmissionFile.isPresent()){
            return ApiResponse.internalServerError("SubmissionFile already exists");
        }

        //将新的文件发给OCR微服务器保存
        Submission submission=submissionRepository.findById(submissionId).get();
        Long assignmentId= submission.getAssignment().getId();
        Optional<Assignment> assignment=assignmentRepository.findById(assignmentId);
        if(assignment.isPresent()){
            boolean needOCR=assignment.get().getNeedOCR();

            if(needOCR && (fileRepository.findById(fileId).get().getFileName().endsWith(".jpeg")
                         ||fileRepository.findById(fileId).get().getFileName().endsWith(".jpg")
                         ||fileRepository.findById(fileId).get().getFileName().endsWith(".png")
                         ||fileRepository.findById(fileId).get().getFileName().endsWith(".pdf"))){
                //将文件发送至OCR服务器
                ocrService.sendFileToOcr(fileId); // fileId 已包含在请求中
            }
        }else{
            return ApiResponse.internalServerError("Assignment does not exist");
        }

        submissionFile.setId(null); // 确保 ID 为 null，让数据库生成
        submissionFile.setSubmission(submissionRepository.getReferenceById(submissionId));
        submissionFile.setFile(fileRepository.getReferenceById(fileId));

        SubmissionFile savedSubmissionFile = submissionFileRepository.save(submissionFile);

        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(savedSubmissionFile.getId());

        logService.createLog("create SubmissionFile: "+ savedSubmissionFile.getId());
        return ApiResponse.ok(postReturnData);
    }

    // 删除一个 SubmissionFile
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isSubmissionFileOwner(authentication, #id)")
    public ResponseEntity<?> deleteSubmissionFile(@PathVariable Long id) {
        Optional<SubmissionFile> optionalSubmissionFile = submissionFileRepository.findById(id);
        if (optionalSubmissionFile.isPresent()) {
            submissionFileRepository.deleteById(id);

            logService.createLog("Delete Submission File: "+id);
            return ApiResponse.ok();
        } else {
            return ApiResponse.internalServerError("SubmissionFile not found");
        }
    }
}