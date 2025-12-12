package runnershi.runnershi.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true),
                @Index(name = "idx_users_nickname", columnList = "nickname", unique = true)
        }

)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    @Column(length = 2)
    private String countryCode;

    @Column(length = 20)
    private String regionCode;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    // ==== 정적 팩토리 메서드 ====

    public static User createdLocalUser(String email,
                                        String encodedPassword,
                                        String nickname,
                                        String countryCode,
                                        String regionCode) {
        OffsetDateTime now = OffsetDateTime.now();
        return User.builder()
                .email(email)
                .passwordHash(encodedPassword)
                .nickname(nickname)
                .countryCode(countryCode)
                .regionCode(regionCode)
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // 추후 닉네임 변경, 상태 변경 등의 도메인 메서드를 여기에 추가
}
