package runnershi.runnershi.domain.user.dto;

public record AuthResponse(
        Long userId,
        String email,
        String nickname
        // Todo: 나중에 accessToken, refreshToken 추가 예정
) {}
