package com.example.restservice.controller;

import com.example.restservice.model.ApiResponse;
import com.example.restservice.model.Role;
import com.example.restservice.model.User;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.model.PostReturnData;
import com.example.restservice.service.LogService;
import com.example.restservice.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    private final LogService logService;

    public UserController(UserRepository userRepository, UserService userService, LogService logService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.logService = logService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        user.setId(null);
        user.setPassword(userService.encodePassword(user.getPassword()));
        User savedUser = userRepository.save(user);
        PostReturnData postReturnData = new PostReturnData();
        postReturnData.setId(savedUser.getId());

        logService.createLog("Create User: "+savedUser.getId());
        return ApiResponse.ok(postReturnData);
    }

    // AI-generated-content
    // tool: ChatGPT
    // version: latest
    // usage: generate based on UserController
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createUsersFromFile(@RequestParam("file") MultipartFile file) {
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
            List<User> users = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                // Skip header row if present
                String line = reader.readLine();
                if (line == null) {
                    return ApiResponse.badRequest("File is empty");
                }

                // Process each line
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("username") && line.toLowerCase().contains("email")) {
                        continue;
                    }

                    String[] data = line.split(",", -1); // Handle empty fields
                    if (data.length < 2) { // Adjust based on expected columns
                        continue; // Skip invalid rows
                    }

                    // Create user from CSV data
                    User user = new User();
                    user.setId(null); // Ensure ID is null for new users
                    user.setUsername(data[0].trim());
                    user.setEmail(data[1].trim());
                    user.setPassword(userService.encodePassword(data[2].trim()));
                    user.setRole(Role.valueOf(data[3].trim()));

                    // Basic validation
                    if (user.getUsername().isEmpty() || user.getEmail().isEmpty()) {
                        continue; // Skip invalid users
                    }

                    users.add(user);
                }
            } catch (IOException e) {
                return ApiResponse.internalServerError("Error reading file: " + e.getMessage());
            }

            if (users.isEmpty()) {
                return ApiResponse.badRequest("No valid users found in the file");
            }

            // Save users in batch
            List<User> savedUsers = userRepository.saveAll(users);

            // Prepare response
            List<PostReturnData> returnData = savedUsers.stream()
                    .map(user -> {
                        PostReturnData data = new PostReturnData();
                        data.setId(user.getId());
                        return data;
                    })
                    .collect(Collectors.toList());

            logService.createLog("Create Users from file: "+ file.getName());
            return ApiResponse.ok(returnData);
        } catch (Exception e) {
            return ApiResponse.internalServerError("Failed to process batch user creation: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();

        logService.createLog("Get all users");
        return ApiResponse.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);

        logService.createLog("Get users by Id: "+id);
        return user.map(ApiResponse::ok).orElseGet(ApiResponse::internalServerError);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ApiResponse.internalServerError();
        }
        User user = optionalUser.get();
        if (userDetails.getPassword() != null) {
            userDetails.setPassword(userService.encodePassword(userDetails.getPassword()));
        }
        user.updateWithoutId(userDetails);
        User updatedUser = userRepository.save(user);

        logService.createLog("Update user: "+id);
        return ApiResponse.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ApiResponse.internalServerError();
        }
        userRepository.deleteById(id);

        logService.createLog("Delete user: "+id);
        return ApiResponse.ok();
    }
}
