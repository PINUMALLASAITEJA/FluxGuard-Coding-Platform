package com.codingplatform;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.codingplatform.model.Problem;
import com.codingplatform.repository.ProblemRepository;

@SpringBootTest
@AutoConfigureMockMvc
class CodingPlatformMvpIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

        @Autowired
        private ProblemRepository problemRepository;

    @Test
    void coreCodingPlatformWorkflowWorks() throws Exception {
        String email = "mvp-user@example.com";

        mockMvc.perform(post("/register")
                        .param("fullName", "MVP User")
                        .param("email", email)
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        var session = mockMvc.perform(post("/login")
                        .param("username", email)
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn()
                .getRequest()
                .getSession();

        mockMvc.perform(post("/problems")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .param("title", "MVP Problem")
                        .param("difficulty", "Easy")
                        .param("description", "Add two values")
                        .param("inputFormat", "Two integers")
                        .param("outputFormat", "Their sum"))
                .andExpect(status().is3xxRedirection());

        Problem problem = problemRepository.findAll().get(0);

        mockMvc.perform(get("/problems/" + problem.getId())
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(view().name("problem-detail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Two integers")));

        mockMvc.perform(post("/problems/update/" + problem.getId())
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .param("title", "Updated MVP Problem")
                        .param("difficulty", "Medium")
                        .param("description", "Updated description"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/submit")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .param("problemId", problem.getId().toString())
                        .param("language", "Java")
                        .param("code", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("problem-detail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Please enter your solution code.")));

        mockMvc.perform(post("/submit")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .param("problemId", problem.getId().toString())
                        .param("language", "Java")
                        .param("code", "return a + b;"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/problems"));

        mockMvc.perform(get("/problems").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(view().name("problems"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("MVP Problem")));

        mockMvc.perform(get("/submissions").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(view().name("submissions"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Updated MVP Problem")));

        mockMvc.perform(post("/problems")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .param("title", "Delete Me")
                        .param("difficulty", "Easy"))
                .andExpect(status().is3xxRedirection());
        Problem problemToDelete = problemRepository.findAll().stream()
                .filter(candidate -> "Delete Me".equals(candidate.getTitle()))
                .findFirst()
                .orElseThrow();
        mockMvc.perform(post("/problems/delete/" + problemToDelete.getId())
                        .session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/dashboard").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard.html"));

        mockMvc.perform(post("/logout").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
