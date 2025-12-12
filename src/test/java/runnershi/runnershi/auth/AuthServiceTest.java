package runnershi.runnershi.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import runnershi.runnershi.domain.user.User;
import runnershi.runnershi.domain.user.UserRepository;
import runnershi.runnershi.domain.user.dto.AuthResponse;
import runnershi.runnershi.domain.user.dto.LoginRequest;
import runnershi.runnershi.domain.user.dto.SignupRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Nested
    @DisplayName("회원가입(signup)")
    class SignupTests {

        @Test
        @DisplayName("정상 회원가입 - 이메일/닉네임 중복이 없으면 유저가 저장된다")
        void signup_success() {
            //given
            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "password123",
                    "runnerPeter",
                    "KR",
                    "KR-11"
            );

            given(userRepository.existsByEmail("test@example.com")).willReturn(false);
            given(userRepository.existsByNickname("runnerPeter")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("EncodedPassword");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            given(userRepository.save(userCaptor.capture()))
                    .willAnswer(invention -> {
                        User u = userCaptor.getValue();
                        return User.builder()
                                .id(1L)
                                .email(u.getEmail())
                                .passwordHash(u.getPasswordHash())
                                .nickname(u.getNickname())
                                .countryCode(u.getCountryCode())
                                .regionCode(u.getRegionCode())
                                .status(u.getStatus())
                                .createdAt(u.getCreatedAt())
                                .updatedAt(u.getUpdatedAt())
                                .build();
                    });
            // when
            AuthResponse response = authService.signup(request);

            // then
            assertThat(response.userId()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.nickname()).isEqualTo("runnerPeter");

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
            assertThat(savedUser.getPasswordHash()).isEqualTo("EncodedPassword");
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일이 이미 존재하면 예외 발생")
        void signup_fail_email_exist() {
            // given
            SignupRequest request = new SignupRequest(
                    "dup@example.com",
                    "password123",
                    "runnerPeter",
                    "KR",
                    "KR-11"
            );

            given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 사용 중인 이메일");
        }

        @Test
        @DisplayName("회원가입 실패 - 닉네임이 이미 존재하면 예외 발생")
        void signup_fail_nickname_exist() {
            SignupRequest request = new SignupRequest(
                    "test@example.com",
                    "password123",
                    "dupNickname",
                    "KR",
                    "KR-11"
            );

            given(userRepository.existsByEmail("test@example.com")).willReturn(false);
            given(userRepository.existsByNickname("dupNickname")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 사용 중인 닉네임");
        }
    }

    @Nested
    @DisplayName("로그인(Login)")
    class LoginTests {

        @Test
        @DisplayName("정상 로그인 - 이메일과 비밀번호가 일치하면 AuthResponse를 반환한다")
        void login_success() {
            // given
            LoginRequest request = new LoginRequest(
                    "login@example.com",
                    "password123"
            );

            User user = User.builder()
                    .id(10L)
                    .email("login@example.com")
                    .passwordHash("EncodedPassword")
                    .nickname("runnerLogin")
                    .status("ACTIVE")
                    .build();

            given(userRepository.findByEmail("login@example.com"))
                    .willReturn(Optional.of(user));

            given(passwordEncoder.matches("password123", "EncodedPassword"))
                    .willReturn(true);

            // when
            AuthResponse response = authService.login(request);
            ;

            // then
            assertThat(response.userId()).isEqualTo(10L);
            assertThat(response.email()).isEqualTo("login@example.com");
            assertThat(response.nickname()).isEqualTo("runnerLogin");
        }

        @Test
        @DisplayName("로그인 실패 - 해당 이메일의 유저가 없으면 예외 발생")
        void login_fail_user_not_found() {
            // given
            LoginRequest request = new LoginRequest(
                    "notfound@example.com",
                    "password123"
            );

            given(userRepository.findByEmail("notfound@example.com"))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("로그인 실패 - passwordHash가 null이면(소셜 전용 계정) 예외 발생")
        void login_fail_no_password_set() {
            // given
            LoginRequest request = new LoginRequest(
                    "social-only@example.com",
                    "password123"
            );

            User socialOnlyUser = User.builder()
                    .id(20L)
                    .email("social-only@example.com")
                    .passwordHash(null)
                    .status("ACTIVE")
                    .build();

            given(userRepository.findByEmail("social-only@example.com"))
                    .willReturn(Optional.of(socialOnlyUser));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("로컬 로그인 비밀번호가 설정되지 않은 계정");
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호가 일치하지 않으면 예외 발생")
        void login_fail_wrong_password() {
            // given
            LoginRequest request = new LoginRequest(
                    "login@example.com",
                    "wrongPassword"
            );

            User user = User.builder()
                    .id(10L)
                    .email("login@example.com")
                    .passwordHash("EncodedPassword")
                    .nickname("runnerLogin")
                    .status("ACTIVE")
                    .build();

            given(userRepository.findByEmail("login@example.com"))
                    .willReturn(Optional.of(user));

            given(passwordEncoder.matches("wrongPassword", "EncodedPassword"))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
        }
    }
}
