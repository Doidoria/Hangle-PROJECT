# 프론트엔드 (피드백)

| 담당자 | HTML | CSS | JavaScript |
|--------|-------|------|-------------|
| 윤태현 | 로그인, 회원가입 | 중앙 위치(뷰로 지정) | - |
| 서동성 | 메인, result | - | - |
| 전의환 | 사용자 프로필 | 버튼 {border-radius: 10px} | - |
| 최승호 | 데이터셋 | - | - |
| 장지원 | 데이터셋 | - | Setting - Tab |
| 이혜연 | 대회 | - | - |


---

# 백엔드 (파트 분배) - REST, SECURITY, REDIS 사용
> 프론트엔드 파트 연결하여 → 백엔드 처리

| 담당자 | 대표 클래스(예시) | DB(예시) | 결과 |
|--------|------------------|-----------|--------|
| 윤태현 | UserController, CreateController, NoticeBoardController | user, Create_tbl, DataBoard_tbl | 로그인, 회원가입(인증)처리, notebook 생성 & 저장, 데이터 보드 기록 & 저장 |
| 서동성 | MainController, DataVisualizationController | DV_tbl | 데이터 시각화 처리 |
| 전의환 | ProfileController | Profile_tbl | 사용자별 프로필 작성 |
| 최승호 | DataSetController | DataSet_tbl | 데이터셋(내용) 기록 & 저장 |
| 장지원 | DataSetController | DataSet_tbl | 데이터셋(내용) 기록 & 저장 |
| 이혜연 | CompetitionsController | COmpetitions_tbl | 대회(내용) 생성 & 저장 |


---

### ⚙️ 추가 안내

> 역할은 고정되어 있지 않으며, 표의 내용은 임의로 작성된 예시입니다.  
> (클래스, 테이블명은 수정 가능)

- 로그인, 회원가입(인증) 백엔드 처리는 **개인별 해보시는 것 추천** - 기본  
- 완료 기한 : **예상 11.17까지 백엔드 처리 (클래스 + DB(REDIS) + REACT)**

---

## 💾 데이터 보관

- **DB** : 영구적인 데이터 보관  
- **Redis** : 캐시, 민감한 정보, 기능에서 잠시 사용하고 제거할 데이터  

---

## 📒 Notebook 생성 작업 진행 중

- **방식** : Docker Jupyter Server 생성하여 외부에서 받아오는 방식  
- **연관 작업**
  - **서동성** : notebook의 생성된 데이터를 받아서 **데이터 시각화 처리**
  - **최승호** : 사용자별 notebook 데이터를 받아서 **데이터셋에 (전체뷰 생성)**
  - **장지원** : notebook의 생성된 데이터를 받아서 **사용자별 데이터셋 페이지 생성**

---

# 🧠 백엔드 (피드백)

| 담당자 | 클래스(종류) | 피드백 |
|--------|---------------|---------|
| 윤태현 |  |  |
| 서동성 |  |  |
| 전의환 |  |  |
| 최승호 |  |  |
| 장지원 |  |  |
| 이혜연 |  |  |

