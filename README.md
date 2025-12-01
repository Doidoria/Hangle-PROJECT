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
| **9. 팀 구성(Contributors)** | 팀 프로젝트라면 역할 명시 | - FE: 조원 전체 할당 (UI/UX, React)<br> - BE: 조원 전체 할당 (API, DB 설계) |
| **10. 개발 일정 및 관리(Timeline/Management)** | 진행 일정, 사용한 협업툴 등 | - Notion으로 일정 관리<br> - GitHub Projects로 이슈 트래킹, Jira를 통한 개발 기록 및 피드백 |
| **11. 배포 주소 및 시연 링크(Demo / Deployment)** | 실제 서비스 링크 또는 영상 | https://www.hangle.store |
| **12. 라이선스(License)** | 오픈소스 라이선스 표시 | `MIT License` |

## 시트 2

# Hangle - 머신러닝 데이터 대회 플랫폼

## 1. 프로젝트 소개 (Overview)
Hangle 는 **Spring Boot 백엔드**와 **React 프론트엔드**를 기반으로 한 머신러닝 대회 웹 애플리케이션입니다.
사용자는 각 머신러닝 대회에 참여해 최종 CSV 파일을 제출해 리더보드에서 순위를 확인할 수 있습니다.
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
| **Frontend**     | React, React Router, Axios, SASS/SCSS, SweetAlert2, Jest |
| **Backend**      | Spring Boot, Spring Security, JWT, Swagger, Thymeleaf    |
| **Database**     | MySQL, Redis                                             |
| **DevOps**       | Docker, Docker Compose, Jenkins, Git                     |
| **Tools** | Python, JUnit, IntelliJ IDEA                                   |



---

## 4. 주요 기능 (Features)
| 기능 구분 | 설명 |
|------------|------|
| **회원 관리**   | JWT 기반 AccessToken 발급 및 검증, 회원 정보 수정       |
| **인증/보안**   | Spring Security 6 인증, OAuth2 로그인, Redis 세션 관리   |
| **대회 관리**   | 대회 데이터 생성, 상태 관리, 파일 업로드/다운로드 처리  |
| **리더보드 관리**| 리더보드 데이터 생성, 상태 관리                        |
| **제출/채점(Ml)**| Python 스크립트로 MAE/AUC/F1/ACCURACY/RMSE 지표 계산                 |
| **고객센터(1:1 문의)** | 1:1 문의 생성/상태 관리, 관리자 기능                   |
| **챗봇 / AI 도우미** | Spring AI + Olama 모델 기반 질의 응답                   |
| **배포/운영 환경** | Docker 기반 배포, Jenkins 자동배포화, Nginx 웹 서버, MySQL DB 사용           |


---

## 5. 프로젝트 구조 (Project Structure)

```
 Hangle-Project
├── BN
│   ├── Dockerfile
│   ├── build.gradle
│   ├── gradle
│   ├── gradlew
│   ├── gradlew.bat
│   ├── settings.gradle
│   └── src
├── CHATBOT
│   ├── Dockerfile
│   └── start.sh
├── CMD.txt
├── DB
│   └── Dockerfile
├── FN
│   ├── Dockerfile
│   ├── build
│   ├── nginx.conf
│   ├── node_modules
│   ├── package-lock.json
│   ├── package.json
│   ├── public
│   └── src
├── JENKINS
│   ├── Dockerfile
│   ├── INFO
│   └── pipeline
├── ML_Scripts
│   ├── helpers
│   ├── score_accuracy.py
│   ├── score_auc.py
│   ├── score_custom.py
│   ├── score_f1.py
│   ├── score_mae.py
│   └── score_rmse.py
├── README.md
├── REDIS
│   └── Dockerfile
├── docker-compose.yml
└── docker-compose_local.yml
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
| FN | 윤태현 | 로그인/회원가입, 마이프로필, 사용자 설정(정보 변경), 챗봇 UI 구현 |
| FN | 전익환 | 고객센터 사용자(1:1 문의 , 나의 문의 목록) 관리자(문의 확인, 검색) |
| FN | 서동성 | 메인페이지 UI, 사용자 대회 페이지(리스트, 대회 상세), 관리자 대회 페이지 (리스트, 수정, 생성) UI 구현 |
| FN | 장지원 | 대회 페이지(유저, 상세 페이지) UI, 생성 페이지 파일 업로드 |
| FN | 이혜연 | 리더보드 페이지(검색, 필터링, 페이징) UI 구현 |
| BN | 윤태현 | JWT 인증, Redis, 사용자 관리&권한 설정, 카카오 로그인, 휴대폰 인증 API, 챗봇(사용자, 개발자 전용), 스웨거 문서화 |
| BN | 전익환 | 1:1 문의 작성, 문의 목록, 문의 목록 조회 및 수정·삭제, 관리자 문의 관리 |
| BN | 서동성 | 대회 페이지(생성, 조회, 수정, 삭제)  |
| BN | 장지원 | 대회 페이지 생성 시 파일 업로드, 대회 제출 및 제출 파일 다운로드, 업로드 파일 저장 경로 생성 |
| BN | 이혜연 | 리더보드 내 최고 점수 관리, 재제출 처리, 실시간 순위 재계산, 검색·필터링 |
| Infra | 윤태현 | AWS EC2 설정, Docker 환경, Docker Compose, 배포 자동화(Jenkins) |

---

## 11. 배포 주소 (Deployment)
- Web: [https://www.hangle.store](https://www.hangle.store)  
- API: [https://api.myplanner.com](https://api.myplanner.com)

---

## 12. 라이선스 (License)
이 프로젝트는 MIT License 하에 배포됩니다.

---

## 13. 회고 (Retrospective)
| 이름   | 느낀점 |
|---|---|
| 윤태현 | 저희 팀은 다른 조보다 늦게 프로젝트를 시작했음에도 불구하고, Jira를 활용한 체계적인 이슈 트래킹, Notion을 통한 효율적인 정보 공유, 그리고 GitHub을 통한 견고한 버전 관리 및 협업 프로세스 덕분에 니즈를 극대화한 프로젝트를 성공적으로 배포할 수 있었습니다! 이 과정에는 많은 우여곡절이 있었지만, 팀원들과의 긴밀한 소통과 문제 해결 노력을 통해 성공적으로 극복해 나갔습니다.특히 React를 사용하며, 프론트엔드 개발의 효율성과 최적화 지원 기능의 이점을 직접 체감할 수 있었습니다. React의 컴포넌트 기반 아키텍처는 프론트엔드의 백엔드 로직의 명확한 분리를 가능하게 하여, 코드의 유지보수성과 관리 용이성을 크게 향상시켰습니다.또한, 서비스 배포(인프라) 과정에서는 네트워크 및 보안 관련 지식을 실무적으로 습득하는 귀중한 경험을 했고, 인프라 구축의 중요성과 함께 안정적인 서비스 제공을 위한 깊이 있는 이해를 키울 수 있었습니다. 이러한 경험을 기반으로 장기적인 내 커리어 진로 방향을 설정할 수 있었고 앞으로도 다양한 개발 방식과 기술 스택을 탐구하며 지속적으로 경험과 지식을 확장해 나가는 개발자로 성장하고 싶습니다. |
| 전익환 | 고객센터용 문의 기능을 구현하면서, 백엔드 요청 처리 과정에서 데이터 매핑 오류와 API 검증 실패 등 다양한 문제 때문에 작업을 진행하는 데 어려움이 있었습니다.하지만, 이러한 문제들은 오히려 코드를 더 꼼꼼하게 검토하고 고민함으로써 해결해 나갔고, 그 과정에서 철저한 API 설계와 디테일한 검증 절차의 중요성을 깨달을 수 있었습니다. |
| 서동성 | 저는 이번 프로젝트에 대해 부분을 많이 참여하지는 못했습니다. 대략 본인이 구현한 것이 아닌데도 그러나본인이 만든 파일을 작업하는 경우가 빈번하게 생성되는 이유로 인해, 파일 충돌이 많이 발생했습니다. 이렇듯 예상하지 못한 문제들이 많았습니다. 그래서 본인이 구현한 부분에 대해서 설명하고, 서로 소통하며 충돌을 줄여 나갔습니다. 팀 프로젝트에서 소통의 중요성을 많이 배웠습니다. |
| 장지원 | 프로젝트 초반 프론트엔드와 백엔드 간의 역할 분담이 되어 함께 작업했는데 프론트엔드 부분을 정확히 맞는 과정이 더욱 힘든 점을 느꼈다. 병합 과정에서 생긴미흡한 엘토로 같은 작은 문제들이 생겨, DTO/엔티티 불일치 등 작은 차이가 기능 오류상황에 큰 영향을 미쳐, 결국 서비스 동작에 문제가 생겼다. 이번 프로젝트로 정확한 역할에 대한 이해를 바탕으로 프론트 또는 백엔드간의 정확한 분담에 대해 고민해야겠다는 생각이 들었다. |
| 이혜연 | 프론트엔드 개발 도중 백엔드 데이터가 어떻게 흐름으로 가져 사용자가 화면에 반영되는지 파악할 수 있었습니다. 그로 인해 프론트와 백엔드가 서로의 구조를 밀접하게 고려하여 작업하는 것이 깨달게 되어, 다음 개발에는 구조 설계를 더 신경 써야겠다는 생각이 들었습니다. |

