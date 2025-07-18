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


| 번호 | 기능        | 메서드    | URL                         | 설명                                         | 요청 예시                                                                                                                                                                | 응답 예시                                                                                                                                                                               |
| -- | --------- | ------ | --------------------------- | ------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1  | 동물 등록     | POST   | `/adoptmate/animals`        | 유기동물 정보 등록 (관리자)                           | `{ "species": "dog", "gender": "male", "color": "brown", "age": 3, "breed": "bulldog", "status": "available", "imgUrl": "https://storage.example.com/animal1.jpg" }` | `{ "animalId": 1, "species": "dog", "gender": "male", "color": "brown", "age": 3, "breed": "bulldog", "status": "available", "imgUrl": "https://storage.example.com/animal1.jpg" }` |
| 2  | 동물 전체 조회  | GET    | `/adoptmate/animals`        | 모든 유기동물 목록 조회                              | 없음                                                                                                                                                                   | `[ { "animalId": 1, "species": "dog", "age": 3, "status": "available" }, ... ]`                                                                                                     |
| 3  | 동물 상세 조회  | GET    | `/adoptmate/animals/{id}`   | 특정 동물 상세 조회                                | 없음                                                                                                                                                                   | `{ "animalId": 1, "species": "dog", "gender": "male", "color": "brown", "age": 3, "breed": "bulldog", "status": "available", "imgUrl": "https://storage.example.com/animal1.jpg" }` |
| 4  | 동물 수정     | PUT    | `/adoptmate/animals/{id}`   | 동물 정보 수정 (관리자)                             | `{ "color": "black", "status": "adopted", "imgUrl": "https://storage.example.com/animal1_updated.jpg" }`                                                             | `{ "animalId": 1, "color": "black", "status": "adopted", "imgUrl": "https://storage.example.com/animal1_updated.jpg" }`                                                             |
| 5  | 동물 삭제     | DELETE | `/adoptmate/animals/{id}`   | 동물 정보 삭제 (관리자)                             | 없음                                                                                                                                                                   | `{ "message": "Animal deleted successfully." }`                                                                                                                                     |
| 6  | 입양 신청     | POST   | `/adoptmate/adoptions`      | 사용자가 특정 동물에 입양 신청                          | `{ "animalId": 1, "memberId": 100, "applyDate": "2025-07-01T10:00:00Z" }`                                                                                            | `{ "adoptionId": 10, "status": "PENDING" }`                                                                                                                                         |
| 7  | 입양 전체 조회  | GET    | `/adoptmate/adoptions`      | 입양 신청 목록 전체 조회 (관리자)                       | 없음                                                                                                                                                                   | `[ { "adoptionId": 10, "animalId": 1, "memberId": 100, "status": "PENDING" }, ... ]`                                                                                                |
| 8  | 입양 상세 조회  | GET    | `/adoptmate/adoptions/{id}` | 특정 입양 신청 상세 조회                             | 없음                                                                                                                                                                   | `{ "adoptionId": 10, "animalId": 1, "memberId": 100, "applyDate": "...", "status": "PENDING", "interview": null }`                                                                  |
| 9  | 입양 상태 변경  | PATCH  | `/adoptmate/adoptions/{id}` | 입양 승인/거절 처리 (관리자)                          | `{ "status": "APPROVED" }`                                                                                                                                           | `{ "adoptionId": 10, "status": "APPROVED" }`                                                                                                                                        |
| 10 | 입양 신청 취소  | DELETE | `/adoptmate/adoptions/{id}` | 사용자가 입양 신청 취소                              | 없음                                                                                                                                                                   | `{ "message": "Adoption request cancelled." }`                                                                                                                                      |
| 11 | 게시글 작성    | POST   | `/adoptmate/posts`          | 사용자가 게시글 작성                                | `{ "title": "입양 후기", "content": "저희 강아지 잘 지내요.", "images": [ "https://storage.example.com/postimg1.jpg", "https://storage.example.com/postimg2.jpg" ] }`             | `{ "postId": 5, "title": "입양 후기", "content": "저희 강아지 잘 지내요.", "images": [...], "authorId": 100, "createdAt": "2025-07-01T10:00:00Z" }`                                              |
| 12 | 게시글 전체 조회 | GET    | `/adoptmate/posts`          | 게시글 목록 전체 조회                               | 없음                                                                                                                                                                   | `[ { "postId": 5, "title": "입양 후기", "authorId": 100, "createdAt": "..." }, ... ]`                                                                                                   |
| 13 | 게시글 상세 조회 | GET    | `/adoptmate/posts/{id}`     | 특정 게시글 상세 조회                               | 없음                                                                                                                                                                   | `{ "postId": 5, "title": "입양 후기", "content": "...", "authorId": 100, "createdAt": "..." }`                                                                                          |
| 14 | 게시글 수정    | PUT    | `/adoptmate/posts/{id}`     | 게시글 수정 (작성자 본인)                            | `{ "title": "수정된 제목", "content": "수정된 내용", "images": [ "https://storage.example.com/postimg1_updated.jpg" ] }`                                                       | `{ "postId": 5, "title": "수정된 제목", "content": "수정된 내용", "images": [...] }`                                                                                                          |
| 15 | 게시글 삭제    | DELETE | `/adoptmate/posts/{id}`     | 게시글 삭제 (작성자 본인)                            | 없음                                                                                                                                                                   | `{ "message": "Post deleted successfully." }`                                                                                                                                       |
| 16 | 이미지 업로드   | POST   | `/adoptmate/uploads/images` | 게시글 이미지 등록<br>📦 `@RequestBody` JSON 형식 사용 | `{ "imageUrl": "https://storage.example.com/abc.jpg" }`                                                                                                              | `{ "imageUrl": "https://storage.example.com/abc.jpg" }`                                                                                                                             |


