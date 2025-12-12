package runnershi.runnershi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import runnershi.runnershi.domain.user.User;
import runnershi.runnershi.domain.user.UserRepository;
import runnershi.runnershi.domain.user.dto.AuthResponse;
import runnershi.runnershi.domain.user.dto.LoginRequest;
import runnershi.runnershi.domain.user.dto.SignupRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // 1) 이메일 중복 체크
        if(userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2) 닉네임 중복 체크
        if(userRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 3) 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 4) 유저 생성 & 저장
        User user = User.createdLocalUser(
                request.email(),
                encodedPassword,
                request.nickname(),
                request.countryCode(),
                request.regionCode()
        );

        // 5) 응답 DTO 반환
        User saved = userRepository.save(user);

        return new AuthResponse(saved.getId(), saved.getEmail(), saved.getNickname());
    }


    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 1) 이메일로 유저 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 2) 비밀번호 검증
        if(user.getPasswordHash() == null) {
            // 소셜 전용 계정일 수도 있음
            throw new IllegalArgumentException("로컬 로그인 비밀번호가 설정되지 않은 계정입니다.");
        }

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3) 로그인 성공 -> 나중에 여기서 JWT 발급 예정
        return new AuthResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
