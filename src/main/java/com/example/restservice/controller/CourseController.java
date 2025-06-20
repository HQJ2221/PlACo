package com.example.restservice.controller;

import com.example.restservice.model.ApiResponse;
import com.example.restservice.model.Course;
import com.example.restservice.repository.CourseRepository;
import com.example.restservice.model.PostReturnData;
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
@RequestMapping("/courses")
public class CourseController {
    private final CourseRepository courseRepository;

    private final LogService logService;

    public CourseController(CourseRepository courseRepository, LogService logService) {
        this.courseRepository = courseRepository;
        this.logService = logService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        course.setId(null);
        Course savedCourse = courseRepository.save(course);
        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(savedCourse.getId());

        logService.createLog("Create Course: "+savedCourse.getName());
        return ApiResponse.ok(postReturnData);
    }

    // AI-generated-content
    // tool: ChatGPT
    // version: latest
    // usage: generate based on Course entity and UserController batch import
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCoursesFromFile(@RequestParam("file") MultipartFile file) {
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
            List<Course> courses = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                // Skip header row if present
                String line = reader.readLine();
                if (line == null) {
                    return ApiResponse.badRequest("File is empty");
                }

                // Process each line
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("name")) {
                        continue; // Skip header row
                    }

                    String[] data = line.split(",", -1); // Handle empty fields
                    if (data.length < 1) { // Adjust based on expected columns
                        continue; // Skip invalid rows
                    }

                    // Create course from CSV data
                    Course course = new Course();
                    course.setId(null); // Ensure ID is null for new courses
                    course.setName(data[0].trim());

                    // Basic validation
                    if (course.getName().isEmpty()) {
                        continue; // Skip invalid courses
                    }

                    courses.add(course);
                }
            } catch (IOException e) {
                return ApiResponse.internalServerError("Error reading file: " + e.getMessage());
            }

            if (courses.isEmpty()) {
                return ApiResponse.badRequest("No valid courses found in the file");
            }

            // Save courses in batch
            List<Course> savedCourses = courseRepository.saveAll(courses);

            // Prepare response
            List<PostReturnData> returnData = savedCourses.stream()
                    .map(course -> {
                        PostReturnData data = new PostReturnData();
                        data.setId(course.getId());
                        return data;
                    })
                    .collect(Collectors.toList());

            logService.createLog("Create Courses from file: " + file.getOriginalFilename());
            return ApiResponse.ok(returnData);
        } catch (Exception e) {
            return ApiResponse.internalServerError("Failed to process batch course creation: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        List<Course> courses = courseRepository.findAll();

        logService.createLog("Get All Courses");
        return ApiResponse.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        Optional<Course> course = courseRepository.findById(id);

        logService.createLog("Get Course by id"+id);
        return course.map(ApiResponse::ok).orElseGet(ApiResponse::internalServerError);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isEmpty()) {
            return ApiResponse.internalServerError();
        }
        Course course = optionalCourse.get();
        course.updateWithoutId(courseDetails);
        Course updatedCourse = courseRepository.save(course);

        logService.createLog("Update Course: " + id);
        return ApiResponse.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        if (!courseRepository.existsById(id)) {
            return ApiResponse.internalServerError();
        }
        courseRepository.deleteById(id);

        logService.createLog("Delete Course: " + id);
        return ApiResponse.ok();
    }
}
