package com.example.restservice.controller;

import com.example.restservice.model.*;
import com.example.restservice.repository.AssignmentRepository;
import com.example.restservice.repository.CourseRepository;
import com.example.restservice.repository.CourseUserRepository;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {
    private static final Logger log = LoggerFactory.getLogger(AssignmentController.class);
    private final AssignmentRepository assignmentRepository;

    private final UserRepository userRepository;

    private final CourseRepository courseRepository;
    private final CourseUserRepository courseUserRepository;

    private final LogService logService;

    public AssignmentController(AssignmentRepository assignmentRepository, UserRepository userRepository, CourseRepository courseRepository, CourseUserRepository courseUserRepository, LogService logService) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseUserRepository = courseUserRepository;
        this.logService=logService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.canCreateAssignment(authentication, #assignment)")
    public ResponseEntity<?> createAssignment(@RequestBody Assignment assignment) {
        assignment.setId(null);
        User user = userRepository.getReferenceById(assignment.getUser().getId());
        Course course = courseRepository.getReferenceById(assignment.getCourse().getId());
        assignment.setUser(user);
        assignment.setCourse(course);
        assignmentRepository.save(assignment);

        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(assignment.getId());

        logService.createLog("Create Assignment: " + assignment.getId());

        return ApiResponse.ok(postReturnData);
    }

    @GetMapping(params = {"user-id"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isUser(authentication, #userId)")
    public ResponseEntity<?> getAssignmentsByUserId(@RequestParam(name = "user-id") Long userId) {
//        List<Assignment> assignments = assignmentRepository.findByUser_Id(userId);

        //找到所有他参与的所有课程，返回其中的所有作业
        List<CourseUser> allCourseUsers=courseUserRepository.findByUser_Id(userId);
        List<Assignment> assignments=new ArrayList<>();
        for (CourseUser courseUser : allCourseUsers) {
            List<Assignment> currentCourseAssignment=assignmentRepository.findByCourse_Id(courseUser.getCourse().getId());
            assignments.addAll(currentCourseAssignment);
        }

        logService.createLog("Get all assignment of user: "+userId);

        return ApiResponse.ok(assignments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.inCourseByAssignment(authentication, #id)")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long id) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);

        logService.createLog("Get Assignment by id: "+id);

        return assignment.map(ApiResponse::ok).orElseGet(ApiResponse::internalServerError);
    }

    @GetMapping(params = {"course-id"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.inCourse(authentication, #courseId)")
    public ResponseEntity<?> getAssignmentsByCourseId(@RequestParam(name = "course-id") Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourse_Id(courseId);

        logService.createLog("Get all assignment of course: "+courseId);

        return ApiResponse.ok(assignments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isTeacherByAssignment(authentication, #id)")
    public ResponseEntity<?> updateAssignment(@PathVariable Long id, @RequestBody Assignment assignmentDetails) {
        Optional<Assignment> optionalAssignment = assignmentRepository.findById(id);
        if (optionalAssignment.isEmpty()) {
            return ApiResponse.internalServerError();
        }
        Assignment assignment = optionalAssignment.get();
        assignment.updateWithoutId(assignmentDetails);
        Assignment updatedAssignment = assignmentRepository.save(assignment);

        logService.createLog("Update Assignment: "+assignment.getId());

        return ApiResponse.ok(updatedAssignment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isAssignmentOwner(authentication, #id)")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        if (!assignmentRepository.existsById(id)) {
            return ApiResponse.internalServerError();
        }
        assignmentRepository.deleteById(id);

        logService.createLog("Delete Assignment: "+id);
        return ApiResponse.ok();
    }
}
