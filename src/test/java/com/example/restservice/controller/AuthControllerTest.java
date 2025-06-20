package com.example.restservice.controller;

import com.example.restservice.TestUtil;
import com.example.restservice.model.User;
import com.example.restservice.repository.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@ExtendWith(RestDocumentationExtension.class)
public class AuthControllerTest {
    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("User Can Log In with Username and Password")
    public void testUserLoginWithUsernameAndPassword() {
        Assertions.assertDoesNotThrow(() -> {
            User user = TestUtil.generateRandomUser();
            userRepository.save(user);
            JSONObject json = new JSONObject();
            json.put("username", user.getUsername());
            json.put("password", TestUtil.getPasswordFromHashedPassword(user.getPassword()));
            mockMvc.perform(post("/auth/login")
                            .content(json.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andReturn();
        });
    }

    @Test
    @DisplayName("User Can Retrieve Their Own Profile")
    public void userCanLoginUsingUsernameAndPassword() {
        Assertions.assertDoesNotThrow(() -> {
            User user = TestUtil.generateRandomUser();
            userRepository.save(user);
            mockMvc.perform(get("/auth/me").with(user(user)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                    .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                    .andExpect(jsonPath("$.data.role").value(user.getRole().toString()))
                    .andReturn();
        });
    }
}
