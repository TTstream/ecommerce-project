package com.portfolio.commerceflow.member.api;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.commerceflow.member.application.RefreshTokenStore;
import com.portfolio.commerceflow.member.infrastructure.MemberRepository;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestRefreshTokenStore refreshTokenStore;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        refreshTokenStore.clear();
    }

    @Test
    void loginReturnsAccessToken() throws Exception {
        signup("user@example.com", "password1", "tester");

        Map<String, String> request = Map.of(
                "email", "USER@example.com",
                "password", "password1"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(1800))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(1209600))
                .andExpect(jsonPath("$.error", nullValue()));
    }

    @Test
    void loginRejectsInvalidPassword() throws Exception {
        signup("user@example.com", "password1", "tester");

        Map<String, String> request = Map.of(
                "email", "user@example.com",
                "password", "wrong-password"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void meRejectsRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void meReturnsAuthenticatedMemberProfile() throws Exception {
        signup("user@example.com", "password1", "tester");
        String accessToken = loginAndExtractAccessToken("user@example.com", "password1");

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.name").value("tester"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void reissueRotatesRefreshTokenAndReturnsNewTokens() throws Exception {
        signup("user@example.com", "password1", "tester");
        JsonNode loginResponse = login("user@example.com", "password1");
        String oldRefreshToken = loginResponse.get("data").get("refreshToken").asText();

        Map<String, String> request = Map.of("refreshToken", oldRefreshToken);

        String response = mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newRefreshToken = objectMapper.readTree(response).get("data").get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", newRefreshToken))))
                .andExpect(status().isOk());
    }

    @Test
    void reissueRejectsAccessToken() throws Exception {
        signup("user@example.com", "password1", "tester");
        String accessToken = loginAndExtractAccessToken("user@example.com", "password1");

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", accessToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    void logoutDeletesRefreshToken() throws Exception {
        signup("user@example.com", "password1", "tester");
        JsonNode loginResponse = login("user@example.com", "password1");
        String accessToken = loginResponse.get("data").get("accessToken").asText();
        String refreshToken = loginResponse.get("data").get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error", nullValue()));

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
    }

    private void signup(String email, String password, String name) throws Exception {
        Map<String, String> request = Map.of(
                "email", email,
                "password", password,
                "name", name
        );

        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private String loginAndExtractAccessToken(String email, String password) throws Exception {
        return login(email, password).get("data").get("accessToken").asText();
    }

    private JsonNode login(String email, String password) throws Exception {
        Map<String, String> request = Map.of(
                "email", email,
                "password", password
        );

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    @TestConfiguration
    static class AuthControllerTestConfig {

        @Bean
        @Primary
        TestRefreshTokenStore testRefreshTokenStore() {
            return new TestRefreshTokenStore();
        }
    }

    static class TestRefreshTokenStore implements RefreshTokenStore {

        private final Map<Long, String> tokens = new ConcurrentHashMap<>();

        @Override
        public void save(Long memberId, String refreshToken, Duration ttl) {
            tokens.put(memberId, refreshToken);
        }

        @Override
        public Optional<String> findByMemberId(Long memberId) {
            return Optional.ofNullable(tokens.get(memberId));
        }

        @Override
        public void deleteByMemberId(Long memberId) {
            tokens.remove(memberId);
        }

        void clear() {
            tokens.clear();
        }
    }
}
