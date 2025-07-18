# 🐾 AdoptMate - 입양동물 플랫폼

유기동물과 입양희망자를 연결하는 풀스택 웹 플랫폼입니다.  
React와 Spring Boot를 기반으로 개발되었으며, 입양 신청부터 후기 등록까지 하나의 흐름으로 제공하는 시스템입니다.

## 🌱 프로젝트 개요

> "기술로 유기동물 문제를 해결할 수 없을까?"라는 고민에서 출발한 개인 프로젝트입니다.  
보호소에서 봉사하며 느꼈던 현실적인 불편함을 바탕으로, 입양 절차를 온라인으로 쉽게 진행할 수 있도록 만들었습니다.

---

## 🔧 기술 스택

### Frontend
- React
- Axios
- React Router
- CSS Modules

### Backend
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL


---

## 🚀 주요 기능

### ✅ 사용자
- 회원가입 / 로그인
- 이메일 인증 / 비밀번호 재설정
- 입양 동물 목록 조회
- 입양 신청 / 후기 등록
- 댓글 기능 (대댓글 설계 포함)

### ✅ 관리자
- 입양 승인 / 반려 기능
- 동물 정보 등록 / 수정
- 사용자 관리

---

## DB 설계
<img width="1280" height="952" alt="image" src="https://github.com/user-attachments/assets/250cbc1b-0326-459e-a89d-17a871cc97be" />
## 유스케이스 다이어그램
<img width="1104" height="930" alt="image" src="https://github.com/user-attachments/assets/ee9125c5-c1a1-4dbd-a8b3-63ffeee61a5d" />

## 백엔드 패키지구조
com.kindtail.adoptmate
├── member
│   ├── controller
│   ├── service
│   ├── repository
│   ├── domain       ← Member 엔티티 클래스
│   └── dto
├── comment
│   ├── controller
│   ├── service
│   ├── repository
│   ├── domain       ← Comment 엔티티 클래스
│   └── dto
├── animal
│   ├── controller
│   ├── service
│   ├── repository
│   ├── domain       ← Animal 엔티티 클래스
│   └── dto
├── adoption
│   ├── controller
│   ├── service
│   ├── repository
│   ├── domain       ← Adoption 엔티티 클래스
│   └── dto
├── board
│   ├── controller
│   ├── service
│   ├── repository
│   ├── domain       ← Board(Post) 엔티티 클래스
│   └── dto
├── config
└── Application.java
## API  명세서
