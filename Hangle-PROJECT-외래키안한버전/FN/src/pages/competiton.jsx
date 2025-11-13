import { useRef } from "react";
import { Link } from 'react-router-dom';
import Layout from './Layout.jsx'
import '../css/competiton.scss'

const Competiton = () => {
  const fileInputRef = useRef(null)

  const submitFile = () => {
    const file = fileInputRef.current?.files[0];
    if (!file) return alert("CSV 파일을 선택하세요.");
    alert(`"${file.name}" 제출 완료! 점수 계산 중...`);
    // setTimeout(() => (location.href = "/leaderboard"), 1200);
  }

  const renderSection = () => (
    <div className="grid">
      {/* 대회 설명 */}
      <article className="card">
        <span className="card-title">대회 정보</span>
        <h3>설명</h3>
        <p className="muted">주어진 이미지 데이터셋을 사용해 고양이/강아지를 분류하는 모델을 학습하세요. Accuracy 기준으로 평가됩니다.</p>
        <ul style={{ marginLeft: "5px" }}>
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
        <a href="./data/train.csv" className="link">train.csv 다운로드</a>
        <br />
        <a href="./data/test.csv" className="link">test.csv 다운로드</a>
      </article>
      {/* 제출 */}
      <article className="card">
        <span className="card-title">제출</span>
        <h3>결과 제출</h3>
        <p className="muted">예측 결과 CSV를 업로드하면 점수가 자동 계산됩니다.</p>
        <input type="file" ref={fileInputRef} accept=".csv" style={{ margin: "10px 0" }} />
        <button className="btn" onClick={submitFile}>제출하기</button>
      </article>
    </div>
  );

  return (
    <>
      <section className="section-wrap">
        <div className="competiton-title">
          <div>
            <h1>이미지 분류 챌린지 🏁</h1>
            <p>Transfer Learning으로 고양이·강아지를 분류하세요!</p>
          </div>
        </div>
        {renderSection()}
      </section>

      <section className="section-wrap">
        <div className="competiton-title">
          <div>
            <h1>이미지 분류 챌린지 🏁</h1>
            <p>Transfer Learning으로 고양이·강아지를 분류하세요!</p>
          </div>
        </div>
        {renderSection()}
      </section>
    </>
  );
};

export default Competiton;
