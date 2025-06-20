package com.example.restservice.auth.service;

import com.example.restservice.model.*;
import com.example.restservice.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class MySecurityService {

    private Authentication authentication;

    private final UserRepository userRepository;

    private final CourseRepository courseRepository;

    private final AssignmentRepository assignmentRepository;

    private final SubmissionRepository submissionRepository;

    private final FileRepository fileRepository;

    private final SubmissionFileRepository submissionFileRepository;

    private final AssignmentFileRepository assignmentFileRepository;

    private final CourseUserRepository courseUserRepository;

    private final ScheduleRepository scheduleRepository;

    public MySecurityService(UserRepository userRepository,
                             CourseRepository courseRepository,
                             AssignmentRepository assignmentRepository,
                             SubmissionRepository submissionRepository,
                             FileRepository fileRepository,
                             SubmissionFileRepository submissionFileRepository,
                             AssignmentFileRepository assignmentFileRepository,
                             CourseUserRepository courseUserRepository,
                             ScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.fileRepository = fileRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.assignmentFileRepository = assignmentFileRepository;
        this.courseUserRepository = courseUserRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public boolean isSubmissionOwner(Authentication authentication, Long submissionId) {
        try {
            Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);
            if (optionalSubmission.isEmpty()) {
                return false;
            }
            Submission submission = optionalSubmission.get();
            Long ownerId = submission.getUser().getId();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return ownerId.equals(user.getId());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAssignmentOwner(Authentication authentication, Long assignmentId) {
        try {
            Optional<Assignment> optionalAssignment = assignmentRepository.findById(assignmentId);
            if (optionalAssignment.isEmpty()) {
                return false;
            }
            Assignment assignment = optionalAssignment.get();
            Long ownerId = assignment.getUser().getId();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return ownerId.equals(user.getId());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAssignmentFileOwner(Authentication authentication, Long assignmentFileId) {
        try {
            Optional<AssignmentFile> optionalAssignmentFile = assignmentFileRepository.findById(assignmentFileId);
            if (optionalAssignmentFile.isEmpty()) {
                return false;
            }

            AssignmentFile assignmentFile=optionalAssignmentFile.get();

            Optional<Assignment> optionalAssignment= assignmentRepository.findById(assignmentFile.getAssignment().getId());
            if (optionalAssignment.isEmpty()) {
                return false;
            }

            Assignment assignment = optionalAssignment.get();
            Long ownerId = assignment.getUser().getId();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return ownerId.equals(user.getId());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSubmissionFileOwner(Authentication authentication, Long submissionFileId) {
        try {
            Optional<SubmissionFile> optionalSubmissionFile = submissionFileRepository.findById(submissionFileId);
            if (optionalSubmissionFile.isEmpty()) {
                return false;
            }
            SubmissionFile submissionFile = optionalSubmissionFile.get();
            Long ownerId = submissionFile.getSubmission().getUser().getId();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return ownerId.equals(user.getId());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTeacherByAssignment(Authentication authentication, Long assignmentId) {
        try {
            Optional<Assignment> optionalAssignment = assignmentRepository.findById(assignmentId);
            if (optionalAssignment.isEmpty()) {
                return false;
            }
            long courseId= optionalAssignment.get().getCourse().getId();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                // 在CourseUser中检查是否是教师
                Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, user.getId());
                if (courseUser.isPresent()) {
                    return courseUser.get().getRole() == RoleInCourse.INSTRUCTOR;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTeacherBySubmission(Authentication authentication, Long submissionId) {
        try {
            Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);
            if (optionalSubmission.isEmpty()) {
                return false;
            }
            long courseId= optionalSubmission.get().getAssignment().getCourse().getId();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                // 在CourseUser中检查是否是教师
                Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, user.getId());
                if (courseUser.isPresent()) {
                    return courseUser.get().getRole() == RoleInCourse.INSTRUCTOR;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTeacherBySubmissionFile(Authentication authentication, Long submissionFileId) {
        try {

            Optional<SubmissionFile> optionalSubmissionFile = submissionFileRepository.findById(submissionFileId);
            if (optionalSubmissionFile.isEmpty()) {
                return false;
            }

            long submissionId=optionalSubmissionFile.get().getSubmission().getId();

            Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);
            if (optionalSubmission.isEmpty()) {
                return false;
            }
            long courseId= optionalSubmission.get().getAssignment().getCourse().getId();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                // 在CourseUser中检查是否是教师
                Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, user.getId());
                if (courseUser.isPresent()) {
                    return courseUser.get().getRole() == RoleInCourse.INSTRUCTOR;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canCreateSubmission(Authentication authentication, Submission submission){
        //需要验证：1 是否是用户本人操作 2 用户是否在当前课程中 3 是否在ddl之前
        try{
            //通过submission获取作业id
            Assignment assignment=submission.getAssignment();
            //通过assignmentId获取课程id
            long courseId=submission.getAssignment().getCourse().getId();
            //通过submission获取用户id
            long userId=submission.getUser().getId();

            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                //判断是否是用户本人操作
                if(user.getId()!=userId){
                    return false;
                }

                //判断当前用户是否在课程中
                Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, user.getId());
                if (courseUser.isEmpty()) {
                    return false;
                }

                //判断当前是否在ddl之前
                return !LocalDateTime.now().isAfter(assignment.getDueDate());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean canCreateAssignment(Authentication authentication, Assignment assignment){
        //需要验证：1 是否是本人操作 2 用户是否是当前课程的教师 3 当前时间<dueDate
        try{
            //通过assignmentId获取课程id
            long courseId=assignment.getCourse().getId();
            //通过submission获取用户id
            long userId=assignment.getUser().getId();

            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                //判断是否是用户本人操作
                if(user.getId()!=userId){
                    return false;
                }

                //判断当前用户是否是教师
                Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, user.getId());
                if (courseUser.isPresent() && courseUser.get().getRole() != RoleInCourse.INSTRUCTOR) {
                    return false;
                }

                //判断当前是否在ddl之前
                return LocalDateTime.now().isBefore(assignment.getDueDate());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean inCourse(Authentication authentication, long courseId){
        try{
            //判断当前用户是否在课程内，无论是老师还是学生
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                //判断当前用户是否在课程中
                Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, user.getId());
                if (courseUser.isEmpty()) {
                    return false;
                }

                return true;
            }
            return false;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean inCourseByAssignment(Authentication authentication, long assignmentId){
        try{

            Optional<Assignment> assignment=assignmentRepository.findById(assignmentId);
            if (assignment.isEmpty()) {
                return false;
            }
            long courseId=assignment.get().getCourse().getId();
            //判断当前用户是否在课程内，无论是老师还是学生
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                //判断当前用户是否在课程中
                Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, user.getId());
                if (courseUser.isEmpty()) {
                    return false;
                }

                return true;
            }
            return false;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean inCourseByAssignmentFile(Authentication authentication, long assignmentFileId){
        try{
            Optional<AssignmentFile> assignmentFile=assignmentFileRepository.findById(assignmentFileId);
            if(assignmentFile.isEmpty()){
                return false;
            }
            long assignmentId=assignmentFile.get().getId();

            Optional<Assignment> assignment=assignmentRepository.findById(assignmentId);
            if (assignment.isEmpty()) {
                return false;
            }
            long courseId=assignment.get().getCourse().getId();
            //判断当前用户是否在课程内，无论是老师还是学生
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                //判断当前用户是否在课程中
                Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, user.getId());
                if (courseUser.isEmpty()) {
                    return false;
                }

                return true;
            }
            return false;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean isAssignmentOwnerByAssignmentFile(Authentication authentication, AssignmentFile assignmentFile){
        try{
            //判断当前用户是否可以创建AssignmentFile
            //1. 当前作业是否是用户创建的
            try {
                Optional<Assignment> optionalAssignment = assignmentRepository.findById(assignmentFile.getAssignment().getId());
                if (optionalAssignment.isEmpty()) {
                    return false;
                }
                Assignment assignment = optionalAssignment.get();
                Long ownerId = assignment.getUser().getId();
                Object principal = authentication.getPrincipal();
                if (principal instanceof User user) {
                    return ownerId.equals(user.getId());
                }
                return false;
            } catch (Exception e) {
                return false;
            }

        }catch (Exception e) {
            return false;
        }
    }

    public boolean isUser(Authentication authentication, long userId){
        try{
            //判断是否是当前用户在操作
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getId() == userId;
            }
            return false;
        }catch (Exception e) {
            return false;
        }
    }

    public boolean isScheduleOwner(Authentication authentication, long scheduleId){
        try{
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getId()==scheduleRepository.findById(scheduleId).get().getUser().getId();
            }
            return false;
        }catch (Exception e) {
            return false;
        }
    }
}
