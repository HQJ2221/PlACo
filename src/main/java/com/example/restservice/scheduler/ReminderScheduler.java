package com.example.restservice.scheduler;

import com.example.restservice.model.*;
import com.example.restservice.repository.AssignmentRepository;
import com.example.restservice.repository.CourseUserRepository;
import com.example.restservice.repository.SubmissionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import com.example.restservice.service.MailService;
import java.time.Duration;

@Component
public class ReminderScheduler {

    private final AssignmentRepository assignmentRepository;
    private final CourseUserRepository courseUserRepository;
    private final SubmissionRepository submissionRepository;
    private final MailService mailService;

    public ReminderScheduler(AssignmentRepository assignmentRepository,
                             CourseUserRepository courseUserRepository,
                             SubmissionRepository submissionRepository,
                             MailService mailService) {
        this.assignmentRepository = assignmentRepository;
        this.courseUserRepository = courseUserRepository;
        this.submissionRepository = submissionRepository;
        this.mailService = mailService;
    }

    @Scheduled(cron = "0 0 * * * ?", zone = "Asia/Hong_Kong") // 每天上午10点执行，香港时间
    public void Reminder() {
        // 遍历所有作业

        List<Assignment> allAssignment=assignmentRepository.findAll();

        //为ddl排序
        LocalDateTime now = LocalDateTime.now();

        allAssignment.sort(Comparator.comparing(Assignment::getDueDate).reversed());

        List<Assignment> filteredAndSorted = allAssignment.stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().isAfter(now))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .toList();

        Duration minDuration = Duration.ofHours(23); // 23小时
        Duration maxDuration = Duration.ofHours(24).minusNanos(1); // 23小时59分59秒999毫微秒

        //逐个检查剩下的作业和当前时间差距是否在23时到23时59分59秒
        for (Assignment assignment : filteredAndSorted) {
            LocalDateTime dueDate = assignment.getDueDate();
            if (dueDate != null) {
                Duration duration = Duration.between(now, dueDate);
                if (!duration.isNegative() && duration.compareTo(minDuration) >= 0 && duration.compareTo(maxDuration) <= 0) {
                    //检查该课程所有学生，检查他们是否交了作业
                    Course course = assignment.getCourse();
                    List<CourseUser> courseUsers=courseUserRepository.findByCourse_Id(course.getId());
                    for(CourseUser courseUser:courseUsers){
                        //如果是学生 并没交作业
                        if(courseUser.getRole()== RoleInCourse.STUDENT){
                            // 检查是否有提交
                            List<Submission> submissions=submissionRepository.findByUser_IdAndAssignment_Id(courseUser.getId(), assignment.getId());
                            if(submissions.isEmpty()){
                                //发送邮件提醒
                                String mailBody= String.format("""
Dear student,

We kindly remind you that you haven't submitted your assignment %s of course %s , which will due in 24 hours.

Best,
PlACo
""", assignment.getTitle(), course.getName());

                                mailService.sendMail(courseUser.getUser().getId(), "Reminder of Assignment", mailBody);
                            }
                        }
                    }
                }
            }
        }
    }
}