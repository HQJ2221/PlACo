package com.example.restservice.controller;

import com.example.restservice.model.*;
import com.example.restservice.repository.CourseRepository;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.repository.CourseUserRepository;
import com.example.restservice.service.LogService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/course-users")
public class CourseUserController {
    private final CourseUserRepository courseUserRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LogService logService;

    public CourseUserController(CourseUserRepository courseUserRepository,
                                CourseRepository courseRepository,
                                UserRepository userRepository,
                                LogService logService) {
        this.courseUserRepository = courseUserRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.logService = logService;
    }

    // 创建 CourseUser 关系（将用户添加到课程中）
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCourseUser(@RequestBody CourseUser courseUser) {
        // 检查课程和用户是否存在
        Long courseId = courseUser.getCourse().getId();
        Long userId = courseUser.getUser().getId();
        if (!courseRepository.existsById(courseId) ||
                !userRepository.existsById(userId)) {
            return ApiResponse.internalServerError("Course or User does not exist");
        }

        // 检查是否已存在相同的 CourseUser 记录
        Optional<CourseUser> existingCourseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, userId);
        if (existingCourseUser.isPresent()) {
            return ApiResponse.internalServerError("User is already enrolled in this course");
        }

        courseUser.setId(null); // 确保 ID 为 null，让数据库生成

        // 设置课程和用户的引用
        courseUser.setCourse(courseRepository.getReferenceById(courseId));
        courseUser.setUser(userRepository.getReferenceById(userId));

        CourseUser savedCourseUser = courseUserRepository.save(courseUser);

        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(savedCourseUser.getId());

        logService.createLog("Create Course User: "+savedCourseUser.getId());
        return ApiResponse.ok(postReturnData);
    }

    // AI-generated-content
    // tool: ChatGPT
    // version: latest
    // usage: generate based on CourseUser entity and previous batch import examples
    // username,course_name,role
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCourseUsersFromFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ApiResponse.badRequest("File is empty or not provided");
            }

            // Check file type
            String contentType = file.getContentType();
            if (!"text/plain".equals(contentType) && !"text/csv".equals(contentType)) {
                return ApiResponse.badRequest("Only CSV files are supported");
            }

            // Read and process CSV file
            List<CourseUser> courseUsers = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                // Skip header row if present
                String line = reader.readLine();
                if (line == null) {
                    return ApiResponse.badRequest("File is empty");
                }

                // Process each line
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("username") && line.toLowerCase().contains("course_name") && line.toLowerCase().contains("role")) {
                        continue; // Skip header row
                    }

                    String[] data = line.split(",", -1); // Handle empty fields
                    if (data.length < 3) { // Expect username, course_name, role
                        continue; // Skip invalid rows
                    }

                    String username = data[0].trim();
                    String courseName = data[1].trim();
                    String roleStr = data[2].trim();

                    // Validate user exists
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if (userOpt.isEmpty()) {
                        continue; // Skip if user not found
                    }

                    // Validate course exists
                    Optional<Course> courseOpt = courseRepository.findByName(courseName);
                    if (courseOpt.isEmpty()) {
                        continue; // Skip if course not found
                    }

                    // Validate role
                    RoleInCourse role;
                    try {
                        role = RoleInCourse.valueOf(roleStr);
                    } catch (IllegalArgumentException e) {
                        continue; // Skip invalid role
                    }

                    // Check for existing relationship (to respect unique constraint)
                    if (courseUserRepository.existsByCourse_IdAndUser_Id(courseOpt.get().getId(), userOpt.get().getId())) {
                        continue; // Skip if relationship already exists
                    }

                    // Create CourseUser relationship
                    CourseUser courseUser = new CourseUser();
                    courseUser.setId(null); // Ensure ID is null for new entries
                    courseUser.setUser(userOpt.get());
                    courseUser.setCourse(courseOpt.get());
                    courseUser.setRoleInCourse(role);

                    // Basic validation
                    if (username.isEmpty() || courseName.isEmpty() || roleStr.isEmpty()) {
                        continue; // Skip invalid entries
                    }

                    courseUsers.add(courseUser);
                }
            } catch (IOException e) {
                return ApiResponse.internalServerError("Error reading file: " + e.getMessage());
            }

            if (courseUsers.isEmpty()) {
                return ApiResponse.badRequest("No valid course-user relationships found in the file");
            }

            // Save course-user relationships in batch
            List<CourseUser> savedCourseUsers = courseUserRepository.saveAll(courseUsers);

            // Prepare response
            List<PostReturnData> returnData = savedCourseUsers.stream()
                    .map(courseUser -> {
                        PostReturnData data = new PostReturnData();
                        data.setId(courseUser.getId());
                        return data;
                    })
                    .collect(Collectors.toList());

            logService.createLog("Create CourseUsers from file: " + file.getOriginalFilename());
            return ApiResponse.ok(returnData);
        } catch (Exception e) {
            return ApiResponse.internalServerError("Failed to process batch course-user creation: " + e.getMessage());
        }
    }

    // 获取所有 CourseUser 关系
    @GetMapping
    public ResponseEntity<?> getAllCourseUsers() {
        List<CourseUser> courseUsers = courseUserRepository.findAll();

        logService.createLog("Get all course user");
        return ApiResponse.ok(courseUsers);
    }

    // 根据 ID 获取特定的 CourseUser 关系
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseUserById(@PathVariable Long id) {
        Optional<CourseUser> courseUser = courseUserRepository.findById(id);

        logService.createLog("Get CourseUser by Id: " + id);
        return courseUser.map(ApiResponse::ok).orElseGet(() -> ApiResponse.internalServerError("CourseUser not found"));
    }

    // 根据课程 ID 获取该课程的所有用户
    @GetMapping(params = {"course-id"})
    public ResponseEntity<?> getUsersByCourseId(@RequestParam(name = "course-id") Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            return ApiResponse.internalServerError("Course not found");
        }
        List<CourseUser> courseUsers = courseUserRepository.findByCourse_Id(courseId);

        logService.createLog("Get Users by CourseId: " + courseId);
        return ApiResponse.ok(courseUsers);
    }

    // 根据用户 ID 获取该用户的所有课程
    @GetMapping(params ={ "user-id"})
    public ResponseEntity<?> getCoursesByUserId(@RequestParam(name = "user-id") Long userId) {
        if (!userRepository.existsById(userId)) {
            return ApiResponse.internalServerError("User not found");
        }
        List<CourseUser> courseUsers = courseUserRepository.findByUser_Id(userId);

        logService.createLog("Get Courses by User id: "+userId);
        return ApiResponse.ok(courseUsers);
    }

    //根据用户ID和课程ID获取该CourseUser
    @GetMapping(params = {"user-id", "course-id"})
    public ResponseEntity<?> getCourseUserByUserIdAndCourseId(@RequestParam(name = "user-id") Long userId,
                                                               @RequestParam(name = "course-id") Long courseId) {
        if (!userRepository.existsById(userId) || !courseRepository.existsById(courseId)) {
            return ApiResponse.internalServerError("User or Course not found");
        }
        Optional<CourseUser> courseUser = courseUserRepository.findCourseUserByCourse_IdAndUser_Id(courseId, userId);

        logService.createLog("Get CourseUser by User Id: "+userId +" and CourseId: "+courseId);
        return ApiResponse.ok(courseUser);
    }

    // 更新 CourseUser（只能更改用户在课程中的角色）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCourseUser(@PathVariable Long id, @RequestBody CourseUser courseUserDetails) {
        Optional<CourseUser> optionalCourseUser = courseUserRepository.findById(id);
        if (optionalCourseUser.isEmpty()) {
            return ApiResponse.internalServerError("CourseUser not found");
        }

        CourseUser courseUser = optionalCourseUser.get();
        // 只允许更新角色，其他字段（如 course 和 user）不可变
        if (courseUserDetails.getRole() != null) {
            courseUser.setRoleInCourse(courseUserDetails.getRole());
        }

        CourseUser updatedCourseUser = courseUserRepository.save(courseUser);

        logService.createLog("Update Course User: "+id);
        return ApiResponse.ok(updatedCourseUser);
    }

    // 删除 CourseUser 关系（将用户从课程中移除）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCourseUser(@PathVariable Long id) {
        if (!courseUserRepository.existsById(id)) {
            return ApiResponse.internalServerError("CourseUser not found");
        }
        courseUserRepository.deleteById(id);

        logService.createLog("Delete Course User: "+id);
        return ApiResponse.ok("CourseUser deleted successfully");
    }
}