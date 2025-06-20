package com.example.restservice.controller;

import com.example.restservice.model.ApiResponse;
import com.example.restservice.model.PostReturnData;
import com.example.restservice.model.Schedule;
import com.example.restservice.model.User;
import com.example.restservice.repository.ScheduleRepository;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 日程控制器，负责处理与日程相关的API请求
 */
@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final LogService logService;

    /**
     * 构造函数注入依赖
     */
    public ScheduleController(ScheduleRepository scheduleRepository,
                              UserRepository userRepository,
                              LogService logService) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.logService = logService;
    }

    /**
     * 创建新日程
     * @param schedule 日程数据
     * @return 创建成功的日程ID
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or #schedule.getUser().getId()==authentication.principal.id") // 用户或管理员可以创建日程
    public ResponseEntity<?> createSchedule(@RequestBody Schedule schedule) {
        // 设置创建时间为当前时间
        schedule.setCreateTime(LocalDateTime.now());

        // 检查用户是否存在
        Optional<User> userOptional = userRepository.findById(schedule.getUser().getId());
        if (userOptional.isEmpty()) {
            return ApiResponse.badRequest("No such user");
        }
        schedule.setUser(userOptional.get());

        // 保存日程
        Schedule savedSchedule = scheduleRepository.save(schedule);
        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(savedSchedule.getId());

        // 记录日志
        logService.createLog("Create schedule: " + savedSchedule.getId());
        return ApiResponse.ok(postReturnData);
    }

    /**
     * 获取所有日程（仅管理员）
     * @return 日程列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 仅管理员可获取所有日程
    public ResponseEntity<?> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAll();

        logService.createLog("Get all schedules");
        return ApiResponse.ok(schedules);
    }
    /**
     * 根据ID获取日程
     * @param id 日程ID
     * @return 日程详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isScheduleOwner(authentication, #id)") // 管理员或日程所有者
    public ResponseEntity<?> getScheduleById(@PathVariable Long id) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(id);
        if (scheduleOptional.isEmpty()) {
            return ApiResponse.internalServerError("No such schedule");
        }

        logService.createLog("Get schedule by ID: " + id);
        return ApiResponse.ok(scheduleOptional.get());
    }

    @GetMapping(params = {"user-id"})
    public ResponseEntity<?> getSchedulesByUserId(@RequestParam(name = "user-id") Long userId) {
        List<Schedule> scheduleList = scheduleRepository.findByUser_Id(userId);

        logService.createLog("Get schedule by ID: " + userId);
        return ApiResponse.ok(scheduleList);
    }


    /**
     * 更新日程
     * @param id 日程ID
     * @param scheduleDetails 更新后的日程数据
     * @return 更新后的日程
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isScheduleOwner(authentication, #id)") // 管理员或日程所有者
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody Schedule scheduleDetails) {
        Optional<Schedule> optionalSchedule = scheduleRepository.findById(id);
        if (optionalSchedule.isEmpty()) {
            return ApiResponse.internalServerError("日程不存在");
        }

        Schedule schedule = optionalSchedule.get();
        // 更新除ID外的字段
        schedule.updateWithoutId(scheduleDetails);

        // 如果提供了新的用户ID，验证并更新
        if (scheduleDetails.getUser() != null && scheduleDetails.getUser().getId() != null) {
            Optional<User> userOptional = userRepository.findById(scheduleDetails.getUser().getId());
            if (userOptional.isEmpty()) {
                return ApiResponse.badRequest("No such user");
            }
            schedule.setUser(userOptional.get());
        }

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        logService.createLog("Update schedule: " + id);
        return ApiResponse.ok(updatedSchedule);
    }

    /**
     * 删除日程
     * @param id 日程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @mySecurityService.isScheduleOwner(authentication, #id)") // 管理员或日程所有者
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        if (!scheduleRepository.existsById(id)) {
            return ApiResponse.internalServerError("No such schedule");
        }

        scheduleRepository.deleteById(id);
        logService.createLog("Delete Schedule: " + id);
        return ApiResponse.ok();
    }
}