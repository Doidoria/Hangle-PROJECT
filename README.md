# READMD

## 시트 1

| 구분 | 설명 | 예시 |
|------|------|------|
| **1. 프로젝트 제목(Project Title)** | Hangle ProJect | `# 사용자의 머신러닝 실력 순위를 참고할 수 있는 웹사이트` |
| **2. 프로젝트 소개(Overview)** | (대회별 리더보드/사용자 대상) | “머신러닝 대회를 제공하고, 사용자가 예측 결과 CSV를 제출하면 서버에서 점수를 계산해 리더보드에 등록하는 플랫폼.” |
| **3. 기술 스택(Tech Stack)** | JAVA, 프레임워크, DB, 배포환경 등을 리스트로 표시 | `React`, `Spring Boot`, `MySQL`, `Docker`, `Nginx`, `AWS EC2` |
| **4. 주요 기능(Features)** | 핵심 기능 나열 (예: 회원가입, 로그인, 일정관리 등) | - 회원가입 및 JWT 로그인<br> - 일정 등록/수정/삭제<br> - 관리자 페이지 |
| **5. 프로젝트 구조(Project Structure)** | 폴더 트리나 주요 파일 설명 | `BN/`, `FN/`, `docker-compose.yml` |
| **6. 설치 및 실행 방법(Installation & Run)** | 클론부터 실행까지 단계별 명령어 | ```bash<br>git clone https://github.com/username/project.git<br>cd project<br>npm install && npm start<br>``` |
| **7. 화면 구성(UI Preview)** | 주요 화면 캡처 이미지 또는 GIF | `![메인화면](./images/main.png)` |
| **8. API 문서(API Docs)** | 주요 REST API 엔드포인트 및 설명 | \```bash<br>GET /api/users — 사용자 조회<br>POST /api/login — 로그인<br>\``` |
| **9. 팀 구성(Contributors)** | 팀 프로젝트라면 역할 명시 | - FE: 홍길동 (UI/UX, React)<br> - BE: 김철수 (API, DB 설계) |
| **10. 개발 일정 및 관리(Timeline/Management)** | 진행 일정, 사용한 협업툴 등 | - Notion으로 일정 관리<br> - GitHub Projects로 이슈 트래킹 |
| **11. 배포 주소 및 시연 링크(Demo / Deployment)** | 실제 서비스 링크 또는 영상 | [https://www.hangle.store(https://www.hangle.store) |
| **12. 라이선스(License)** | 오픈소스 라이선스 표시 | `MIT License` |
| **13. 회고(Feedback / Retrospective)** | 프로젝트를 통해 배운 점, 개선할 점 | “Git Flow 전략을 적용하면서 협업의 중요성을 배움.” |

## 시트 2

# MyPlanner - 일정 관리 웹 애플리케이션

## 1. 프로젝트 소개 (Overview)
MyPlanner는 **Spring Boot 백엔드**와 **React 프론트엔드**를 기반으로 한 일정 관리 웹 애플리케이션입니다.  
사용자는 일정을 등록, 수정, 삭제할 수 있으며, 달력 형태로 한눈에 일정 현황을 확인할 수 있습니다.  
JWT 기반 로그인과 권한 관리, REST API를 통해 클라이언트와 서버 간 데이터를 주고받습니다.

---

## 2. 개발 목적
- 풀스택(Spring Boot + React) 구조 이해 및 실습
- JWT 인증/인가 및 RESTful API 구현
- 실시간 일정 관리 시스템 설계 및 배포 경험

---

## 3. 기술 스택 (Tech Stack)
| 구분 | 사용 기술 |
|------|------------|
| Frontend | React 18, Axios, React Router, MUI |
| Backend | Spring Boot 3.x, JPA, Lombok, Spring Security, JWT |
| Database | MySQL 8.x |
| DevOps | Docker, Docker Compose, Nginx, AWS EC2 |
| Tools | IntelliJ, VSCode, GitHub, Postman |

---

## 4. 주요 기능 (Features)
| 기능 구분 | 설명 |
|------------|------|
| 회원 관리 | 회원가입, 로그인, 비밀번호 재설정 (JWT 기반 인증) |
| 일정 관리 | 일정 등록, 수정, 삭제, 조회 |
| 캘린더 보기 | React-Calendar를 통한 월별 일정 시각화 |
| 관리자 기능 | 전체 사용자 일정 관리 및 통계 조회 |
| 배포 환경 | Docker Compose로 Frontend, Backend, DB 통합 실행 |

---

## 5. 프로젝트 구조 (Project Structure)

```
 MyPlanner
├──  backend
│ ├──  src/main/java/com/example/myplanner
│ │ ├── controller/
│ │ ├── service/
│ │ ├── repository/
│ │ ├── entity/
│ │ └── MyPlannerApplication.java
│ └──  src/main/resources
│ ├── application.yml
│ └── static/
│
├──  frontend
│ ├── src/
│ │ ├── components/
│ │ ├── pages/
│ │ ├── api/
│ │ ├── App.js
│ │ └── index.js
│ └── package.json
│
└── docker-compose.yml
```

## 6. 설치 및 실행 (Installation & Run)
### 1) GitHub 저장소 클론
```bash
git clone [https://github.com/username/MyPlanner.git](https://github.com/Doidoria/Hangle-PROJECT.git)
cd MyPlanner
```

### 2) 백엔드 실행
```bash
cd BN
./mvnw spring-boot:run
```

### 3) 프론트엔드 실행
```bash
cd FN
npm install
npm start
```

### 4) Docker Compose로 전체 실행
```bash
docker-compose up -d
```

---

## 7. 화면 구성 (UI Preview)
| 메인 페이지 | 일정 등록 페이지 |
|--------------|----------------|
| ![메인](./images/main.png) | ![등록](./images/add_schedule.png) |

---

## 8. API 문서 (API Docs)
| 메서드 | 엔드포인트 | 설명 |
|--------|-------------|------|
| `POST` | `/api/auth/login` | 로그인 |
| `POST` | `/api/auth/signup` | 회원가입 |
| `GET` | `/api/schedule` | 일정 목록 조회 |
| `POST` | `/api/schedule` | 일정 등록 |
| `PUT` | `/api/schedule/{id}` | 일정 수정 |
| `DELETE` | `/api/schedule/{id}` | 일정 삭제 |

---

## 9. 개발 일정 (Timeline)
| 기간 | 주요 내용 |
|------|------------|
| 2025.10 ~ 2025.12 | 프로젝트 기획 및 화면 설계 |
| 2025.11 | Spring Boot + MySQL 백엔드 구축 |
| 2025.11 | React 프론트엔드 개발 |
| 2025.11 | Docker 기반 통합 테스트 및 배포 |

---

## 10. 팀 구성 (Contributors)
| 역할 | 이름 | 담당 |
|------|------|------|
| FN | 윤태현 | React UI(Layout(Aside, header, myprofile, setting, login, join, main), Axios, OAuth 연동, ThemeContext, ProtectedRoute, ProtOneCert, ChatBot |
| FN | 전익환 | 역할 |
| FN | 서동성 | 역할 |
| FN | 장지원 | 역할 |
| FN | 이혜연 | 역할 |
| FN | 최승호 | 역할 |
| BN | 윤태현 | JWT 인증, Redis, User 관리, 시큐리티 설정, 휴대폰 인증 API, 챗봇(스웨거 문서화) |
| BN | 전익환 | 역할 |
| BN | 서동성 | 역할 |
| BN | 장지원 | 역할 |
| BN | 이혜연 | 역할 |
| BN | 최승호 | 역할 |
| Infra | 윤태현 | Docker Compose, 배포 자동화 |

---

## 11. 배포 주소 (Deployment)
- Web: [https://www.hangle.store](https://www.hangle.store)  
- API: [https://api.myplanner.com](https://api.myplanner.com)

---

## 12. 라이선스 (License)
이 프로젝트는 MIT License 하에 배포됩니다.

---

## 13. 회고 (Retrospective)
- Spring Boot와 React 연동을 직접 경험하면서 **CORS, JWT, 비동기 통신** 구조를 명확히 이해하게 됨.  
- Docker Compose 기반의 **로컬-운영 환경 일원화**의 중요성을 체감.  
- 차후 개선점: 알림 기능 추가, AWS S3 이미지 업로드 기능 구현 예정.
- 
-

