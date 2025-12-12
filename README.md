# Runner's Hi Android Repository

Runner's Hi 프로젝트의 백엔드 리포지토리입니다.

---

## 1. 프로젝트 개요

- **서비스 이름**: Runner's Hi
- **플랫폼**: Android, IOS
- **주요 목적**
    - 러닝 기록 측정 및 히스토리 조회 제공
    - 랭킹 조회 기능 서비스 제공

---

## 2. 기술 스택(Back-end)

-   **Framework:** Spring Boot 3.5.8
-   **Language:** Java 17
-   **Database:** H2(test), PostgreSQL, Redis
-   **ORM:** Spring Data JPA
-   **Security:** JWT
-   **Testing:** JUnit 5, Mockito
-   **API:** Swagger, PostMan
-   **Infra:** AWS, Docker
---

## 3. 주요 기능(Back-end)

- 회원가입 / 로그인 정보 관리
- 프론트엔드에서 전송된 데이터 관리
    - 러닝 기록, 히스토리 목록 저장 및 조회
- 랭킹 조회 데이터 관리
    - 지역별, 일/주/월/년, 성별, 나이 등 랭킹 저장 및 조회
- 추후 커뮤니티 기능 확장 예정
    - 게시판, 프로필, 인플루언서



---

## 4. 프로젝트 구조

```text
backend
├─ .idea
├─ runnershi
│  ├─ .gradle
│  ├─ .idea
│  ├─ gradle
│  ├─ src
│  │  ├─ main
│  │  ├─ test
│  │  │  ├─ res/
│  │  │  └─
│  │  └─ test/ ...
│  ├─ build.gradle
├─ build.gradle
├─ settings.gradle
└─ README.md
```

