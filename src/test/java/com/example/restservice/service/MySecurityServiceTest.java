package com.example.restservice.service;

import com.example.restservice.auth.service.MySecurityService;
import com.example.restservice.model.*;
import com.example.restservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// AI-generated-content
// tool: ChatGPT
// version: latest
// usage: generate based on MySecurityService
@ExtendWith(MockitoExtension.class)
class MySecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private SubmissionFileRepository submissionFileRepository;

    @Mock
    private AssignmentFileRepository assignmentFileRepository;

    @Mock
    private CourseUserRepository courseUserRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MySecurityService mySecurityService;

    private User user;
    private Course course;
    private Assignment assignment;
    private Submission submission;
    private AssignmentFile assignmentFile;
    private SubmissionFile submissionFile;
    private CourseUser courseUser;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        course = new Course();
        course.setId(1L);

        assignment = new Assignment();
        assignment.setId(1L);
        assignment.setUser(user);
        assignment.setCourse(course);
        assignment.setDueDate(LocalDateTime.now().plusDays(1));

        submission = new Submission();
        submission.setId(1L);
        submission.setUser(user);
        submission.setAssignment(assignment);

        assignmentFile = new AssignmentFile();
        assignmentFile.setId(1L);
        assignmentFile.setAssignment(assignment);

        submissionFile = new SubmissionFile();
        submissionFile.setId(1L);
        submissionFile.setSubmission(submission);

        courseUser = new CourseUser();
        courseUser.setCourse(course);
        courseUser.setUser(user);
        courseUser.setRoleInCourse(RoleInCourse.INSTRUCTOR);

        schedule = new Schedule();
        schedule.setId(1L);
        schedule.setUser(user);

        when(authentication.getPrincipal()).thenReturn(user);
    }

    @Test
    void isSubmissionOwner_ValidSubmission_ReturnsTrue() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        boolean result = mySecurityService.isSubmissionOwner(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isSubmissionOwner_DifferentUser_ReturnsFalse() {
        User differentUser = new User();
        differentUser.setId(2L);
        when(authentication.getPrincipal()).thenReturn(differentUser);
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        boolean result = mySecurityService.isSubmissionOwner(authentication, 1L);
        assertFalse(result);
    }

    @Test
    void isAssignmentOwner_ValidAssignment_ReturnsTrue() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        boolean result = mySecurityService.isAssignmentOwner(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isAssignmentFileOwner_ValidAssignmentFile_ReturnsTrue() {
        when(assignmentFileRepository.findById(1L)).thenReturn(Optional.of(assignmentFile));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        boolean result = mySecurityService.isAssignmentFileOwner(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isSubmissionFileOwner_ValidSubmissionFile_ReturnsTrue() {
        when(submissionFileRepository.findById(1L)).thenReturn(Optional.of(submissionFile));
        boolean result = mySecurityService.isSubmissionFileOwner(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isTeacherByAssignment_ValidInstructor_ReturnsTrue() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.isTeacherByAssignment(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isTeacherByAssignment_NonInstructor_ReturnsFalse() {
        courseUser.setRoleInCourse(RoleInCourse.STUDENT);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.isTeacherByAssignment(authentication, 1L);
        assertFalse(result);
    }

    @Test
    void isTeacherBySubmission_ValidInstructor_ReturnsTrue() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.isTeacherBySubmission(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isTeacherBySubmissionFile_ValidInstructor_ReturnsTrue() {
        when(submissionFileRepository.findById(1L)).thenReturn(Optional.of(submissionFile));
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.isTeacherBySubmissionFile(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void canCreateSubmission_ValidSubmission_ReturnsTrue() {
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.canCreateSubmission(authentication, submission);
        assertTrue(result);
    }

    @Test
    void canCreateSubmission_PastDueDate_ReturnsFalse() {
        assignment.setDueDate(LocalDateTime.now().minusDays(1));
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.canCreateSubmission(authentication, submission);
        assertFalse(result);
    }

    @Test
    void canCreateAssignment_ValidInstructor_ReturnsTrue() {
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.canCreateAssignment(authentication, assignment);
        assertTrue(result);
    }

    @Test
    void canCreateAssignment_NonInstructor_ReturnsFalse() {
        courseUser.setRoleInCourse(RoleInCourse.STUDENT);
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.canCreateAssignment(authentication, assignment);
        assertFalse(result);
    }

    @Test
    void inCourse_ValidCourseUser_ReturnsTrue() {
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.inCourse(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void inCourseByAssignment_ValidCourseUser_ReturnsTrue() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.inCourseByAssignment(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void inCourseByAssignmentFile_ValidCourseUser_ReturnsTrue() {
        when(assignmentFileRepository.findById(1L)).thenReturn(Optional.of(assignmentFile));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(courseUserRepository.findCourseUserByCourse_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(courseUser));
        boolean result = mySecurityService.inCourseByAssignmentFile(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isAssignmentOwnerByAssignmentFile_ValidOwner_ReturnsTrue() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        boolean result = mySecurityService.isAssignmentOwnerByAssignmentFile(authentication, assignmentFile);
        assertTrue(result);
    }

    @Test
    void isUser_ValidUser_ReturnsTrue() {
        boolean result = mySecurityService.isUser(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isUser_DifferentUser_ReturnsFalse() {
        boolean result = mySecurityService.isUser(authentication, 2L);
        assertFalse(result);
    }

    @Test
    void isScheduleOwner_ValidOwner_ReturnsTrue() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        boolean result = mySecurityService.isScheduleOwner(authentication, 1L);
        assertTrue(result);
    }

    @Test
    void isScheduleOwner_NonExistentSchedule_ReturnsFalse() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());
        boolean result = mySecurityService.isScheduleOwner(authentication, 1L);
        assertFalse(result);
    }
}