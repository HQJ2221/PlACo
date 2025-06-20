package com.example.restservice.service;

import com.example.restservice.model.User;
import com.example.restservice.model.UserLog;
import com.example.restservice.repository.UserLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogService {

    private final UserLogRepository userLogRepository;

    @Autowired
    public LogService(UserLogRepository userLogRepository) {
        this.userLogRepository = userLogRepository;
    }

    /**
     * 创建一个新的日志条目，使用当前认证用户。
     *
     * @param logMessage 日志消息
     * @return 保存的 UserLog 实体
     * @throws IllegalStateException 如果当前用户未认证
     */
    public UserLog createLog(String logMessage) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) {
            throw new IllegalStateException("当前用户未认证");
        }
        UserLog userLog = new UserLog();
        userLog.setUser(user);
        userLog.setLogs(logMessage);
        userLog.setTime(LocalDateTime.now());
        return userLogRepository.save(userLog);
    }

    public List<UserLog> getAllLogs(){
        return userLogRepository.findAll();
    }
    /**
     * 检索特定用户的所有日志。
     *
     * @param user 要检索日志的用户
     * @return UserLog 实体列表
     */
    public List<UserLog> getLogsByUser(User user) {
        return userLogRepository.findByUser(user);
    }

    /**
     * 检索特定用户在指定时间范围内的日志。
     *
     * @param user 要检索日志的用户
     * @param startTime 时间范围的开始
     * @param endTime 时间范围的结束
     * @return UserLog 实体列表
     */
    public List<UserLog> getLogsByUserAndTimeRange(User user, LocalDateTime startTime, LocalDateTime endTime) {
        return userLogRepository.findByUserAndTimeBetween(user, startTime, endTime);
    }

    /**
     * 检索指定时间范围内的所有日志。
     *
     * @param startTime 时间范围的开始
     * @param endTime 时间范围的结束
     * @return UserLog 实体列表
     */
    public List<UserLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return userLogRepository.findByTimeBetween(startTime, endTime);
    }

    /**
     * 根据日志 ID 删除日志条目。
     *
     * @param logId 要删除的日志 ID
     */
    public void deleteLog(Long logId) {
        userLogRepository.deleteById(logId);
    }
}