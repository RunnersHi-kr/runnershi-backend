package runnershi.runnershi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import runnershi.runnershi.domain.user.dto.AuthResponse;
import runnershi.runnershi.domain.user.dto.LoginRequest;
import runnershi.runnershi.domain.user.dto.SignupRequest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthService authService; // 더 이상 @MockBean이 아님! TestConfig에서 등록됨

    /**
     * Test 전용 Bean 등록
     * Spring Boot 3.4 이상에서는 @MockBean 대신 이런 방식을 사용하라고 권장한다.
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        AuthService authService() {
            return Mockito.mock(AuthService.class);
        }
    }

    @Test
    @DisplayName("회원가입 API - 정상 요청 시 200과 AuthResponse를 반환한다")
    void signup_success() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com",
                "password123",
                "runnerPeter",
                "KR",
                "KR-11"
        );

        AuthResponse response = new AuthResponse(
                1L,
                "test@example.com",
                "runnerPeter"
        );

        given(authService.signup(Mockito.any(SignupRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("runnerPeter"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest(
                "login@example.com",
                "password123"
        );

        AuthResponse response = new AuthResponse(
                10L,
                "login@example.com",
                "runnerLogin"
        );

        given(authService.login(Mockito.any()))
                .willReturn(response);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10L))
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.nickname").value("runnerLogin"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 사용 중인 이메일이면 IllegalArgumentException이 발생한다 (예외 핸들러 미구현 상태)")
    void signup_fail() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "dup@example.com",
                "password123",
                "runnerPeter",
                "KR",
                "KR-11"
        );

        given(authService.signup(Mockito.any()))
                .willThrow(new IllegalArgumentException("이미 사용 중인 이메일입니다."));

        // when & then
        assertThatThrownBy(() ->
                mockMvc.perform(
                                post("/api/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andReturn()   // perform 결과까지 포함해서 하나의 람다로 감싸기
        )
                .isInstanceOf(ServletException.class) // 바깥으로 보이는 예외 타입
                .hasRootCauseInstanceOf(IllegalArgumentException.class) // 실제 원인
                .hasRootCauseMessage("이미 사용 중인 이메일입니다.");
    }
}