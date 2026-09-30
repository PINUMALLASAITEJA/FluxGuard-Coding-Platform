package com.codingplatform;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.codingplatform.model.Problem;
import com.codingplatform.repository.ProblemRepository;
import com.codingplatform.fluxguard.repository.FluxGuardRequestLogRepository;
import com.codingplatform.fluxguard.model.FluxGuardRequestLog;

@SpringBootTest
@AutoConfigureMockMvc
class CodingPlatformMvpIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

        @Autowired
        private ProblemRepository problemRepository;

        @Autowired
        private FluxGuardRequestLogRepository fluxGuardRequestLogRepository;

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

        mockMvc.perform(post("/submit")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .param("problemId", problem.getId().toString())
                        .param("language", "Java")
                        .param("code", "return a + b;"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Duplicate request blocked"));
        org.junit.jupiter.api.Assertions.assertTrue(fluxGuardRequestLogRepository.findAll().stream()
                .anyMatch(log -> "Duplicate request blocked".equals(log.getFailureReason())));

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

        mockMvc.perform(get("/fluxguard").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(view().name("fluxguard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Today's Requests")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Failed Request Details")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Recent Activity"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Login History"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Devices"))));

        mockMvc.perform(post("/logout").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginUsernameInputDoesNotUseHtmlEmailValidation() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("type=\"text\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("type=\"email\""))));
    }

        @Test
        void suspiciousLoginIsDeniedWithoutStoringCredentials() throws Exception {
                mockMvc.perform(post("/login")
                                                .param("username", "' OR '1'='1")
                                                .param("password", "never-store-this-secret"))
                                .andExpect(status().isForbidden())
                                .andExpect(content().string("Action Denied!"));

                var event = fluxGuardRequestLogRepository.findAll().stream()
                                .filter(log -> "Suspicious request detected".equals(log.getFailureReason()))
                                .findFirst()
                                .orElseThrow();
                org.junit.jupiter.api.Assertions.assertEquals("AUTHENTICATION", event.getEventType());
                org.junit.jupiter.api.Assertions.assertFalse(String.valueOf(event.getUsername()).contains("never-store-this-secret"));
                org.junit.jupiter.api.Assertions.assertFalse(String.valueOf(event.getQueryParameters()).contains("never-store-this-secret"));

                mockMvc.perform(post("/login")
                                .param("username", "operator OR analyst")
                                .param("password", "invalid-password"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/login?error"));
        }

            @Test
            void dailyRequestQueriesExcludeButRetainHistoricalFailures() {
                LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
                LocalDateTime tomorrow = today.plusDays(1);
                long requestCountBefore = fluxGuardRequestLogRepository
                        .countByTimestampGreaterThanEqualAndTimestampLessThan(today, tomorrow);
                long failedCountBefore = fluxGuardRequestLogRepository
                        .countByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusGreaterThanEqual(today, tomorrow, 400);

                FluxGuardRequestLog yesterdayFailure = failedRequest(today.minusMinutes(1));
                FluxGuardRequestLog todayFailure = failedRequest(today.plusMinutes(1));
                List<FluxGuardRequestLog> savedLogs = fluxGuardRequestLogRepository.saveAll(List.of(yesterdayFailure, todayFailure));

                org.junit.jupiter.api.Assertions.assertEquals(requestCountBefore + 1,
                        fluxGuardRequestLogRepository.countByTimestampGreaterThanEqualAndTimestampLessThan(today, tomorrow));
                org.junit.jupiter.api.Assertions.assertEquals(failedCountBefore + 1,
                        fluxGuardRequestLogRepository.countByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusGreaterThanEqual(
                                today, tomorrow, 400));
                List<Long> todaysFailureIds = fluxGuardRequestLogRepository
                        .findTop100ByTimestampGreaterThanEqualAndTimestampLessThanAndResponseStatusGreaterThanEqualOrderByTimestampDesc(
                                today, tomorrow, 400).stream()
                        .map(FluxGuardRequestLog::getId)
                        .toList();
                org.junit.jupiter.api.Assertions.assertFalse(todaysFailureIds.contains(savedLogs.get(0).getId()));
                org.junit.jupiter.api.Assertions.assertTrue(todaysFailureIds.contains(savedLogs.get(1).getId()));
                org.junit.jupiter.api.Assertions.assertTrue(fluxGuardRequestLogRepository.existsById(savedLogs.get(0).getId()));
            }

            @Test
            void staticResourceFailuresAreIgnoredFromMeaningfulFailureMetrics() {
                LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
                LocalDateTime tomorrow = today.plusDays(1);

                FluxGuardRequestLog faviconFailure = failedRequest(today.plusHours(1));
                faviconFailure.setEndpoint("/favicon.ico");
                faviconFailure.setResponseStatus(404);
                faviconFailure.setFailureReason("Not Found");

                FluxGuardRequestLog loginFailure = failedRequest(today.plusHours(2));
                loginFailure.setEndpoint("/login");
                loginFailure.setHttpMethod("POST");
                loginFailure.setResponseStatus(401);
                loginFailure.setFailureReason("Invalid credentials");
                loginFailure.setEventType("AUTHENTICATION");

                fluxGuardRequestLogRepository.saveAll(List.of(faviconFailure, loginFailure));

                var meaningfulFailures = fluxGuardRequestLogRepository.findTop100MeaningfulFailuresBetween(today, tomorrow);
                org.junit.jupiter.api.Assertions.assertTrue(meaningfulFailures.stream()
                        .anyMatch(log -> "/login".equals(log.getEndpoint())
                                && "Invalid credentials".equals(log.getFailureReason())));
                org.junit.jupiter.api.Assertions.assertTrue(meaningfulFailures.stream()
                        .noneMatch(log -> "/favicon.ico".equals(log.getEndpoint())));
            }

            private FluxGuardRequestLog failedRequest(LocalDateTime timestamp) {
                FluxGuardRequestLog log = new FluxGuardRequestLog();
                log.setTimestamp(timestamp);
                log.setHttpMethod("POST");
                log.setEndpoint("/test-action");
                log.setResponseStatus(400);
                log.setFailureReason("test failure");
                log.setEventType("REQUEST");
                return log;
            }
}
