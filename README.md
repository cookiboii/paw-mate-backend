# 🐾 PawMate - 입양동물 플랫폼 백엔드

유기동물과 입양희망자를 연결하는 풀스택 웹 플랫폼의 백엔드 서비스입니다.  
Spring Boot와 Java 17을 기반으로 구축되었으며, 회원 관리, 이메일 인증, 카카오 소셜 로그인, JWT/Redis 토큰 관리, 보호 동물의 입양 신청 및 계층형 대댓글 커뮤니티 기능을 제공합니다.

---

## 🌱 프로젝트 개요

> "기술로 유기동물 문제를 해결할 수 없을까?"라는 고민에서 출발한 개인 프로젝트입니다.  
보호소에서 봉사하며 느꼈던 현실적인 불편함을 바탕으로, 입양 절차를 온라인으로 쉽게 진행하고 소통할 수 있도록 제작하였습니다.

---

## 🔧 기술 스택

### Backend Framework & Language
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.3
- **Security & Auth**: Spring Security, OAuth2 Client, JWT (jjwt 0.11.5)
- **Database / ORM**: Spring Data JPA, H2 / MySQL
- **Cache & Session**: Redis (Spring Data Redis)
- **Mail Service**: JavaMailSender (SMTP)
- **Build Tool**: Gradle

---

## 🚀 주요 기능

### 🔐 인증 및 회원 관리
- 일반 회원가입 및 로그인 (JWT Access Token & Refresh Token 기반)
- 이메일 인증 코드 발송 및 검증 (Redis 연동)
- 비밀번호 재설정 기능 (안전한 인증 확인 검증 적용)
- 카카오 OAuth2 소셜 로그인 지원
- Redis 기반 토큰 자동 갱신 및 로그아웃 블랙리스트 관리

### 🐶 보호 동물 관리
- 보호 동물 등록, 상세 조회 및 페이징 목록 조회
- 보호 동물 상태 수정 (보호중, 입양완료 등) 및 삭제 (관리자 권한)

### 🏡 입양 신청 관리
- 유기동물 입양 신청서 제출 및 중복 신청 방지
- 사용자별 내 입양 신청 내역 조회
- 관리자 전체 입양 신청 내역 조회 및 상태 변경 (승인 / 거절)

### 💬 커뮤니티 & 계층형 댓글
- 입양 후기 및 게시글 작성, 조회(페이징), 수정, 삭제
- 계층형 댓글 및 대댓글(답글) 작성/조회 (EntityGraph 페치 조인 적용으로 N+1 쿼리 최적화)

---

## 📊 시스템 구조 및 다이어그램

### DB 설계 (ERD)
<img width="1280" height="952" alt="DB Diagram" src="https://github.com/user-attachments/assets/250cbc1b-0326-459e-a89d-17a871cc97be" />

### 유스케이스 다이어그램
<img width="1104" height="930" alt="Use Case Diagram" src="https://github.com/user-attachments/assets/ee9125c5-c1a1-4dbd-a8b3-63ffeee61a5d" />

---

## 📋 REST API 명세서

### 👤 1. 회원 & 인증 API (`/adoptmate`)

| 메서드 | URL | 권한 | 설명 |
| :--- | :--- | :--- | :--- |
| `POST` | `/adoptmate/register` | Public | 일반 회원가입 |
| `POST` | `/adoptmate/login` | Public | 로그인 (Access & Refresh Token 발급) |
| `POST` | `/adoptmate/refresh-token` | Public | Access Token 재발급 |
| `POST` | `/adoptmate/logout` | User | 로그아웃 (토큰 블랙리스트 및 Redis 파기) |
| `GET` | `/adoptmate/myInfo` | User | 내 정보 조회 |
| `GET` | `/adoptmate/all` | Admin | 전체 회원 목록 조회 |
| `POST` | `/adoptmate/password` | User | 비밀번호 변경 (로그인 상태) |
| `DELETE` | `/adoptmate/delete` | User | 회원 탈퇴 |

### ✉️ 2. 이메일 인증 & 비밀번호 재설정 API (`/adoptmate`)

| 메서드 | URL | 권한 | 설명 |
| :--- | :--- | :--- | :--- |
| `POST` | `/adoptmate/verify-email` | Public | 회원가입용 이메일 인증 코드 발송 |
| `POST` | `/adoptmate/verify-code` | Public | 이메일 인증 코드 검증 |
| `POST` | `/adoptmate/send-reset-code` | Public | 비밀번호 재설정 인증 코드 발송 |
| `POST` | `/adoptmate/verify-reset-code` | Public | 비밀번호 재설정 인증 코드 검증 |
| `PATCH` | `/adoptmate/password` | Public | 비밀번호 재설정 (인증 완료 후) |

### 🔑 3. 카카오 소셜 로그인 API (`/adoptmate`)

| 메서드 | URL | 권한 | 설명 |
| :--- | :--- | :--- | :--- |
| `GET` | `/adoptmate/kakao` | Public | 카카오 OAuth2 콜백 리다이렉트 |

### 🐶 4. 보호 동물 관리 API (`/animals`)

| 메서드 | URL | 권한 | 설명 |
| :--- | :--- | :--- | :--- |
| `POST` | `/animals/register` | Admin | 보호 동물 등록 |
| `GET` | `/animals/list` | Public | 보호 동물 목록 조회 (페이징: `page`, `size`) |
| `GET` | `/animals/{id}` | Public | 보호 동물 상세 조회 |
| `PUT` | `/animals/{id}/status` | Admin | 보호 동물 상태 변경 (보호중/입양완료 등) |
| `DELETE` | `/animals/delete/{id}` | Admin | 보호 동물 삭제 |

### 🏡 5. 입양 신청 관리 API (`/adoptions`)

| 메서드 | URL | 권한 | 설명 |
| :--- | :--- | :--- | :--- |
| `POST` | `/adoptions/animals/{animalId}` | User | 동물 입양 신청 |
| `GET` | `/adoptions/myAdoption` | User | 내 입양 신청 내역 조회 |
| `GET` | `/adoptions/all` | Admin/User | 전체 입양 신청 내역 조회 |
| `PUT` | `/adoptions/{adoptionId}/status` | User | 입양 신청 상태 변경 (승인 / 거절) |

### 📝 6. 커뮤니티 게시글 API (`/post`)

| 메서드 | URL | 권한 | 설명 |
| :--- | :--- | :--- | :--- |
| `POST` | `/post/create` | User | 게시글 작성 |
| `GET` | `/post/list` | Public | 게시글 목록 조회 (페이징: `page`, `size`) |
| `GET` | `/post/{postId}` | Public | 게시글 상세 조회 |
| `PUT` | `/post/{postId}` | Author/Admin | 게시글 수정 |
| `DELETE` | `/post/{postId}` | Author/Admin | 게시글 삭제 |

### 💬 7. 댓글 & 답글 API (`/comment`)

| 메서드 | URL | 권한 | 설명 |
| :--- | :--- | :--- | :--- |
| `POST` | `/comment/{postId}` | User | 댓글 또는 답글 작성 (`parentId` 지정) |
| `GET` | `/comment/{postId}` | Public | 특정 게시글 댓글 목록 조회 (계층형 구조) |
| `PUT` | `/comment/update/{commentId}` | Author/Admin | 댓글 수정 |
| `DELETE` | `/comment/{commentId}` | Author/Admin | 댓글 삭제 |


