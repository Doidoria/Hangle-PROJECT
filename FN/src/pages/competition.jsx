import { useRef } from "react";
import { useNavigate } from "react-router-dom";
import "../css/competition.scss";

const Competition = () => {
  const fileInputRef = useRef(null);
  const navigate = useNavigate();

  const submitFile = () => {
    const file = fileInputRef.current?.files?.[0];
    if (!file) {
      alert("CSV 파일을 선택하세요.");
      return;
    }
    // 간단 검증: 확장자 및 최대 크기(예: 5MB)
    const isCsv = /\.csv$/i.test(file.name);
    const isUnder5MB = file.size <= 5 * 1024 * 1024;
    if (!isCsv) return alert("CSV 파일만 업로드할 수 있습니다.");
    if (!isUnder5MB) return alert("파일 크기는 5MB 이하만 허용됩니다.");

    alert(`"${file.name}" 제출 완료! 점수 계산 중...`);
    // 서버 업로드/채점 호출 자리에 FormData 업로드 넣으면 됨.
    // setTimeout은 데모용. 실제론 업로드 응답을 보고 이동하세요.
    setTimeout(() => navigate("/leaderboard"), 800);
  };

  const Section = () => (
    <div className="grid">
      {/* 대회 설명 */}
      <article className="card">
        <span className="card-title">대회 정보</span>
        <h3>설명</h3>
        <p className="muted">
          주어진 이미지 데이터셋으로 고양이/강아지를 분류하는 모델을 학습하세요. Accuracy 기준으로 평가됩니다.
        </p>
        <ul style={{ marginLeft: 5 }}>
          <li>파일 형식: <code>submission.csv</code></li>
          <li>평가지표: Accuracy</li>
          <li>제출 마감: 2025-12-01</li>
        </ul>
      </article>

      {/* 데이터 다운로드 */}
      <article className="card">
        <span className="card-title">데이터셋</span>
        <h3>데이터 다운로드</h3>
        <p className="muted">Train/Test 파일을 내려받아 학습하세요.</p>
        {/* public/data/train.csv, public/data/test.csv 에 파일 두기 */}
        <a href="/data/train.csv" className="link" download>
          train.csv 다운로드
        </a>
        <a href="/data/test.csv" className="link" download style={{ marginLeft: 12 }}>
          test.csv 다운로드
        </a>
      </article>

      {/* 제출 */}
      <article className="card">
        <span className="card-title">제출</span>
        <h3>결과 제출</h3>
        <p className="muted">예측 결과 CSV를 업로드하면 점수가 자동 계산됩니다.</p>
        <input
          type="file"
          ref={fileInputRef}
          accept=".csv,text/csv"
          aria-label="예측 결과 CSV 업로드"
          style={{ margin: "10px 0" }}
        />
        <button className="btn" onClick={submitFile}>제출하기</button>
      </article>
    </div>
  );

  return (
    <>
      <section className="section-wrap">
        <div className="competition-title">
          <div>
            <h1>이미지 분류 챌린지</h1>
            <p>Transfer Learning으로 고양이·강아지를 분류하세요!</p>
          </div>
        </div>
        <Section />
      </section>

      {/* 동일 섹션을 두 번 노출하려는 의도가 아니라면 아래 블록은 제거 권장 */}
      {/* <section className="section-wrap">
        <div className="competition-title">
          <div>
            <h1>이미지 분류 챌린지</h1>
            <p>Transfer Learning으로 고양이·강아지를 분류하세요!</p>
          </div>
        </div>
        <Section />
      </section> */}
    </>
  );
};

export default Competition;
