package com.portfolio.commerceflow.admin.api;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.portfolio.commerceflow.common.security.JwtTokenProvider;
import com.portfolio.commerceflow.member.domain.Member;
import com.portfolio.commerceflow.member.infrastructure.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void adminApiRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/health"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void adminApiRejectsUserRole() throws Exception {
        Member user = memberRepository.save(Member.createUser(
                "user@example.com",
                passwordEncoder.encode("password1"),
                "user"
        ));
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());

        mockMvc.perform(get("/api/v1/admin/health")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void adminApiAllowsAdminRole() throws Exception {
        Member admin = memberRepository.save(Member.createAdmin(
                "admin@example.com",
                passwordEncoder.encode("password1"),
                "admin"
        ));
        String accessToken = jwtTokenProvider.createAccessToken(admin.getId(), admin.getEmail(), admin.getRole());

        mockMvc.perform(get("/api/v1/admin/health")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ADMIN_UP"))
                .andExpect(jsonPath("$.error", nullValue()));
    }
}
