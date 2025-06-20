package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.Role;
import com.example.restservice.model.User;
import com.example.restservice.repository.UserRepository;
import com.example.restservice.service.UserService;
import jakarta.servlet.ServletException;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class UserControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    public void testUserCreateUser() {
        assertDoesNotThrow(() -> {
            // Create and save a regular user
            User regularUser = TestUtil.generateRandomUser(Role.USER);
            User savedRegularUser = userRepository.save(regularUser);

            mockMvc.perform(post("/users")
                            .with(user(savedRegularUser)) // Use saved regular user
                            .content(new JSONObject().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        });
    }

    @Test
    public void testAdminCreateUser() {
        assertDoesNotThrow(() -> {
            // Create and save an admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create user JSON
            JSONObject json = new JSONObject();
            json.put("username", "user");
            json.put("email", "user@example.com");
            json.put("password", "$2a$04$vN3dRWUJPbAzBTIwSs5nQ.DVLzxMY1F/RDYpIae6WMKAc4PAKfkVC");
            json.put("role", "USER");
            mockMvc.perform(post("/users")
                            .with(user(savedAdmin)) // Use saved admin user
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andReturn();
        });
    }

    @Test
    public void testAdminGetOneUser() {
        assertDoesNotThrow(() -> {
            // Create and save an admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create user
            User newUser = new User();
            newUser.setUsername("user");
            newUser.setEmail("user@example.com");
            newUser.setPassword("$2a$04$vN3dRWUJPbAzBTIwSs5nQ.DVLzxMY1F/RDYpIae6WMKAc4PAKfkVC");
            newUser.setRole(Role.USER);
            User savedNewUser = userRepository.save(newUser);
            // Get user
            mockMvc.perform(get("/users/{id}", savedNewUser.getId())
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").value(savedNewUser.getId()))
                    .andExpect(jsonPath("$.data.username").value(savedNewUser.getUsername()))
                    .andExpect(jsonPath("$.data.email").value(savedNewUser.getEmail()))
                    .andExpect(jsonPath("$.data.role").value(savedNewUser.getRole().toString()))
                    .andDo(document("users/get",
                            pathParameters(
                                    parameterWithName("id").description("The ID of the user")
                            ),
                            responseFields(
                                    fieldWithPath("status").description("Status of the response (e.g., 'success')"),
                                    fieldWithPath("message").ignored(),
                                    fieldWithPath("data").description("User data object"),
                                    fieldWithPath("data.id").description("Unique identifier of the user"),
                                    fieldWithPath("data.username").description("Username of the user"),
                                    fieldWithPath("data.email").description("Email address of the user"),
                                    fieldWithPath("data.password").ignored(),
                                    fieldWithPath("data.role").description("Role of the user (e.g., USER, ADMIN)"),
                                    fieldWithPath("data.userTheme").description("Theme preference of the user (e.g., AUTO, LIGHT, DARK)"),
                                    fieldWithPath("data.authorities").ignored(),
                                    fieldWithPath("data.authorities[].authority").ignored(),
                                    fieldWithPath("data.enabled").ignored(),
                                    fieldWithPath("data.accountNonExpired").ignored(),
                                    fieldWithPath("data.credentialsNonExpired").ignored(),
                                    fieldWithPath("data.accountNonLocked").ignored(),
                                    fieldWithPath("metadata").ignored()
                            )
                    ))
                    .andReturn();
        });
    }

    @Test
    public void testAdminUpdateOneUser() {
        assertDoesNotThrow(() -> {
            // Create and save an admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            // Create user
            User newUser = new User();
            newUser.setUsername("user");
            newUser.setEmail("user@example.com");
            newUser.setPassword("$2a$04$vN3dRWUJPbAzBTIwSs5nQ.DVLzxMY1F/RDYpIae6WMKAc4PAKfkVC");
            newUser.setRole(Role.USER);
            User savedNewUser = userRepository.save(newUser);
            // Update user
            JSONObject json = new JSONObject();
            json.put("username", "updated");
            json.put("role", "ADMIN");
            mockMvc.perform(put("/users/" + savedNewUser.getId())
                            .with(user(savedAdmin)) // Use saved admin user
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.id").value(savedNewUser.getId()))
                    .andExpect(jsonPath("$.data.username").value("updated"))
                    .andExpect(jsonPath("$.data.email").value(savedNewUser.getEmail()))
                    .andExpect(jsonPath("$.data.role").value(Role.ADMIN.toString()))
                    .andReturn();
        });
    }

    // AI-generated-content
    // tool: ChatGPT
    // version: latest
    // usage: generate based on other tests
    @Test
    public void testAdminCreateUserFromCSVFile() {
        assertDoesNotThrow(() -> {
            // Create and save an admin user
            User admin = TestUtil.generateRandomUser(Role.ADMIN);
            User savedAdmin = userRepository.save(admin);

            User user1 = TestUtil.generateRandomUser();
            User user2 = TestUtil.generateRandomUser();

            // Encode passwords using UserService
            String rawPassword1 = TestUtil.getPasswordFromHashedPassword(user1.getPassword());
            String rawPassword2 = TestUtil.getPasswordFromHashedPassword(user2.getPassword());

            // Prepare CSV content using random user data
            String csvContent = String.format(
                    "username,email,password,role\n" +
                            "%s,%s,%s,%s\n" +
                            "%s,%s,%s,%s\n",
                    user1.getUsername(), user1.getEmail(), rawPassword1, user1.getRole().toString(),
                    user2.getUsername(), user2.getEmail(), rawPassword2, user2.getRole().toString()
            );

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "users.csv",
                    MediaType.TEXT_PLAIN_VALUE,
                    csvContent.getBytes()
            );

            // Perform batch upload
            mockMvc.perform(multipart("/users/batch")
                            .file(file)
                            .with(user(savedAdmin))) // Use saved admin user
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").isNumber())
                    .andExpect(jsonPath("$.data[1].id").isNumber())
                    .andReturn();

            // Verify users in database
            assertThat(userRepository.findByUsername(user1.getUsername())).isPresent();
            assertThat(userRepository.findByUsername(user2.getUsername())).isPresent();
            assertThat(userRepository.findByUsername(user1.getUsername()).get().getEmail()).isEqualTo(user1.getEmail());
            assertThat(userRepository.findByUsername(user2.getUsername()).get().getEmail()).isEqualTo(user2.getEmail());
            assertThat(userRepository.findByUsername(user1.getUsername()).get().getRole()).isEqualTo(user1.getRole());
            assertThat(userRepository.findByUsername(user2.getUsername()).get().getRole()).isEqualTo(user2.getRole());
        });
    }
}