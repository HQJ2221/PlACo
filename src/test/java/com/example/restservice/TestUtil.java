package com.example.restservice;

import com.example.restservice.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestUtil {
    public static String generateRandomString() {
        return generateRandomString(10);
    }

    public static String generateRandomString(int targetStringLength) {
        int leftLimit = 48;
        int rightLimit = 122;
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static int generateRandomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static String generateRandomEmail() {
        String[] domains = {"example.com", "test.com", "demo.com", "sample.com"};
        String localPart = generateRandomString(8);
        String domain = domains[new Random().nextInt(domains.length)];
        return localPart + "@" + domain;
    }

    public static String generateRandomBcryptHashedPassword() {
        return "$2a$04$DcQcNZXwihwG7pKJy904Gu5syaXsNaz6iz0ZZwLsU7q4JYdwTiIUi";
    }

    public static String getPasswordFromHashedPassword(String hashedPassword) {
        switch (hashedPassword) {
            case "$2a$04$DcQcNZXwihwG7pKJy904Gu5syaXsNaz6iz0ZZwLsU7q4JYdwTiIUi":
                return "password";
            default:
                throw new IllegalArgumentException("Unknown hashed password");
        }
    }

    public static Role generateRandomRole() {
        Role[] roles = Role.values();
        return roles[new Random().nextInt(roles.length)];
    }

    public static User generateRandomUser() {
        User user = new User();
        user.setUsername(generateRandomString());
        user.setPassword(generateRandomBcryptHashedPassword());
        user.setEmail(generateRandomEmail());
        user.setRole(generateRandomRole());
        return user;
    }

    public static User generateRandomUser(Role role) {
        User user = new User();
        user.setUsername(generateRandomString());
        user.setPassword(generateRandomBcryptHashedPassword());
        user.setEmail(generateRandomEmail());
        user.setRole(role);
        return user;
    }

    public static Course generateRandomCourse() {
        Course course = new Course();
        course.setName(generateRandomString());
        return course;
    }

    public static File generateRandomFile() {
        File file = new File();
        file.setOriginalName(generateRandomString());
        file.setFileName(generateRandomString());
        return file;
    }

    public static Assignment generateRandomAssignment() {
        Assignment assignment = new Assignment();
        assignment.setDueDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        assignment.setFullMark(100);
        assignment.setPublishTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        assignment.setTitle("title");
        assignment.setDescription("description");
        assignment.setMaxAttempts(3);
        assignment.setType(AssignmentType.CODE);
        assignment.setCreateTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        return assignment;
    }

    public static Submission generateRandomSubmission() {
        Submission submission = new Submission();
        submission.setSubmitTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        submission.setScore(100.0F);
        submission.setScoreVisible(true);
        submission.setProgrammingLanguage(ProgrammingLanguage.JAVA_JDK_17_0_6);
        return submission;
    }
}
