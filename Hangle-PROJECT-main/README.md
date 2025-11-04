## 프로젝트명: **Mini Kaggle (Spring Boot + React)**

> 한 개의 머신러닝 대회를 제공하고, 사용자가 예측 결과 CSV를 제출하면
> 
> 
> 서버에서 점수를 계산해 리더보드에 등록하는 플랫폼.
> 

---

## 주요 기능 구조

### 🧑‍💻 유저 관련

- 회원가입 / 로그인 / JWT 인증
- 프로필 페이지 (회원정보 수정)
- MY데이터 (대회 참여 기록)

### 📊 대회 페이지

- 대회 정보 조회 (설명, 규칙, 평가 방식)
- 데이터셋 다운로드 (train.csv / test.csv)
- 제출 CSV 업로드
- 점수 계산 (백엔드에서 Pandas-like 로직 구현)

### 🏆 리더보드

- 사용자별 최고 점수 정렬
- 점수, 닉네임, 제출 횟수 표시

---

## * 점수 계산 로직 (Spring Boot 예시) *

Spring Boot에서는 Python처럼 Pandas를 직접 쓰긴 어렵지만,

**Java CSV Parser(OpenCSV)** 로 데이터를 읽어

**정답 CSV와 비교하여 정확도(accuracy)** 계산할 수 있습니다.

```java
public double evaluateSubmission(File submissionFile, File answerFile) throws IOException {
    List<String[]> sub = new CSVReader(new FileReader(submissionFile)).readAll();
    List<String[]> ans = new CSVReader(new FileReader(answerFile)).readAll();

    int correct = 0;
    int total = Math.min(sub.size(), ans.size());
    for (int i = 1; i < total; i++) { // header 제외
        if (sub.get(i)[1].equals(ans.get(i)[1])) correct++;
    }
    return (double) correct / (total - 1);
}
```

- 이 로직을 **Service Layer**에 넣고, 제출 시 호출되도록 하면 됩니다.

---

## DB 설계 예시 (PostgreSQL 기준)

| 테이블 | 주요 컬럼 | 설명 |
| --- | --- | --- |
| **user** | id, email, password, nickname | 사용자 정보 |
| **competition** | id, title, description, metric | 대회 정보 |
| **submission** | id, user_id, score, created_at, file_path | 제출 내역 |
| **leaderboard_view** | (View) user_id, nickname, max(score) | 리더보드용 뷰 |

---

##  React 페이지 구성 예시

| 경로 | 설명 |
| --- | --- |
| `/` | 메인: 대회 소개 + 리더보드 바로가기 |
| `/competition` | 대회 상세 + 데이터 다운로드 버튼 |
| `/submit` | CSV 업로드 폼 |
| `/leaderboard` | 리더보드 테이블 |
| `/login` / `/join` | 회원 기능 |
| `/profile` | 내 제출 기록 |

---

## Docker 구성 예시

`docker-compose.yml` 예시:

```yaml
version: "3"
services:
  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    depends_on:
      - backend

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/minikaggle
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=1234
    depends_on:
      - db

  db:
    image: postgres:15
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 1234
      POSTGRES_DB: minikaggle
    ports:
      - "5432:5432"
    volumes:
      - db-data:/var/lib/postgresql/data

volumes:
  db-data:
```

---

## ⏳ 개발 일정 (예시: 3주)

| 주차 | 목표 |
| --- | --- |
| **1주차** | 프로젝트 세팅 (Spring Boot, React, Docker Compose) + 로그인 기능 |
| **2주차** | 대회 페이지 + CSV 업로드 + 점수 계산 API |
| **3주차** | 리더보드 완성 + UI 다듬기 + 배포 |

---

## 추가로 확장 가능 아이디어

- 이메일 인증 or OAuth (Google 로그인)
- 여러 대회 지원 (Admin Dashboard 추가)
- 제출 결과 시각화 (그래프)
- “Public / Private 리더보드” 구분
- FastAPI + Spring 통신 (Python으로 점수 계산 전담)
- 대회별 1, 2, 3등 뱃지 부여

---
<aside>

https://www.kaggle.com/ ← 참고(링크)

</aside>

<aside>

## 프론트엔드 (파트 분배)

</aside>

---

| 담당 | HTML | CSS | JavaScript |
| --- | --- | --- | --- |
| **윤태현** | 로그인, 회원가입, 셋팅
대회 목록(전체) | - | Setting - Tab |
| **서동성** | 메인, 대회관리 | - | 다크, 라이트 모드 |
| **전익환** | 사용자 프로필 | - | - |
| **최승호** | 마이데이터 | - | - |
| **장지원** | 대회페이지 | - | - |
| **이혜연** | 리더보드 | - | - |
- **완료 기한 : 10.27 까지 HTML+CSS (Javascript 페이지별 - 추가사항)**

---

https://github.com/Doidoria/Hangle-PROJECT/tree/html

- 위 링크 → html branches → Hangle(샘플) 다운로드 → 작업 시작
    1. 본인 제작 페이지.html
    2. css 폴더 → 본인 제작 페이지.css
    3. `common.css` (통합 css 속성) 참고

---

![image.png](attachment:2fdedadd-ba8c-466e-8937-d62f4501dae2:image.png)
↑ var(--색상 이름) → 색상 사용 방법

---

- **var 속성 지정 사용 이유**
    - **Dark, light** 모두 사용을 위해 기본 `common.css` 파일의 `root`, `theme-dark` 라이브러리에서 색상을 통해 색상 자동 변환 → `main.js`에서 작동

---

<aside>

## 프론트엔드 (피드백)

</aside>

---

| 담당 | HTML | CSS | JavaScript |
| --- | --- | --- | --- |
| **윤태현** | 로그인, 회원가입, 셋팅
대회 목록(전체) | 중앙 위치(뷰로 지정) | - |
| **서동성** | 메인, 대회관리 | - |  |
| **전익환** | 사용자 프로필 | 버튼 {border-radis : 10px} | -  |
| **최승호** | 마이데이터 | - | -  |
| **장지원** | 대회페이지 | - |  Setting - Tab |
| **이혜연** | 리더보드 | - | -  |

<aside>

## 백엔드 (파트 분배) - REST, SECURITY, REDIS 사용

- 프론트엔드 파트 연결하여 → 백엔드 처리
</aside>

---

| 담당 | 대표 클래스(예시) | 스키마 | 테이블(예시) | 결과 |
| --- | --- | --- | --- | --- |
| **윤태현** | UserController, 
CompetitonController | registerdb,
competitondb | user ,leaderboard, competiton | 로그인, 회원가입(인증)처리, 대회 목록 생성 |
| **서동성** | MainController 
CompetitonCreateController | - | - | 메인 |
| **전익환** | ProfileController, SettingController | profiledb, settingdb | profile | 사용자별 프로필 작성,
유저별 셋팅 설정 |
| **최승호** | MyDataController | mydatadb | mydata | 사용자별 데이터 기록 & 저장 |
| **장지원** | CompetitonController | mydatadb | mydata | 대회 생성 |
| **이혜연** | LeaderboardController | leaderboarddb | leaderboard | 리더보드(순위) 생성 |

※ 역할은 고정하되 표의 내용은 임의로 작성한 것 (클래스, 테이블명은 수정 가능)

- 로그인, 회원가입(인증) 백엔드 처리는 개인별 해보시는거 추천! - 기본
- **완료 기한 : 예상 11.17 까지 백엔드 처리 (클래스 + DB(REDIS)+REACT)**

---

- **데이터 보관**
    - **DB** : 영구적인 데이터 보관
    - **Redis** : 캐시, 민감한 정보, 기능에서 잠시 사용하고 제거할 데이터

---

- NoteBook 생성 작업 진행중
    - docker Juypter Server 생성하여 외부에서 받아오는 방식
    - 연관 작업 ↓
        - 서동성 : notebook의 생성된 데이터를 받아서 데이터 시각화 처리 ↓
            - 최승호 : 사용자별 notebook 데이터를 받아서 데이터셋에 (전체뷰 생성)
            - 장지원 : notebook의 생성된 데이터를 받아서 사용자별 데이터셋 페이지 생성

---

<aside>

## 백엔드 (피드백)

</aside>

---

| 담당 | 클래스(종류) | 피드백 |
| --- | --- | --- |
| **윤태현** |  |  |
| **서동성** |  |  |
| **전익환** |  |  |
| **최승호** |  |  |
| **장지원** |  |  |
| **이혜연** |  |  |
