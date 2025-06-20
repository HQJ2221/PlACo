package com.example.restservice.controller;

import com.example.restservice.model.ApiResponse;
import com.example.restservice.model.User;
import com.example.restservice.model.UserLog;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;
    private final UserRepository userRepository;

    public LogController(LogService logService, UserRepository userRepository) {
        this.logService = logService;
        this.userRepository = userRepository;
    }

    /**
     * 管理员获取指定用户的所有日志。
     *
     * @param userId 用户ID
     * @return 指定用户的日志列表
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<?> getLogsByUser(@PathVariable Long userId) {
        try {
            // 假设有一个方法可以根据 userId 获取 User 对象
            User user = userRepository.getUserById(userId);
            List<UserLog> logs = logService.getLogsByUser(user);
            logService.createLog("User: " + user.getId() + " request log");
            return ApiResponse.ok(logs);
        } catch (Exception e) {
            return ApiResponse.internalServerError("Fail: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllLogs() {
        return ApiResponse.ok(logService.getAllLogs());
    }

    @GetMapping("/all/range")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllLogsByTimeRange(
            @RequestParam("start") String startTime,
            @RequestParam("end") String endTime
    ){

        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        return ApiResponse.ok(logService.getLogsByTimeRange(start, end));
    }

    /**
     * 获取指定用户在指定时间范围内的日志，仅限管理员。
     *
     * @param userId 用户ID
     * @param startTime 开始时间 (格式: yyyy-MM-dd'T'HH:mm:ss)
     * @param endTime 结束时间 (格式: yyyy-MM-dd'T'HH:mm:ss)
     * @return 用户在时间范围内的日志列表
     */
    @GetMapping("/user/{userId}/range")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<?> getLogsByUserAndTimeRange(
            @PathVariable Long userId,
            @RequestParam("start") String startTime,
            @RequestParam("end") String endTime) {
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            User user= userRepository.getUserById(userId);
            List<UserLog> logs = logService.getLogsByUserAndTimeRange(user, start, end);
            logService.createLog("Request log: user id: " + userId + ", " + startTime + " to " + endTime);
            return ApiResponse.ok(logs);
        } catch (Exception e) {
            return ApiResponse.badRequest("Fail: " + e.getMessage());
        }
    }
}