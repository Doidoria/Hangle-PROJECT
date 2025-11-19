import { useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/axiosConfig";
import "../css/competition.scss";

const Competition = () => {
  const fileInputRef = useRef(null);
  const navigate = useNavigate();
  const { id: competitionId } = useParams();

  /** 🔥 쿠키에서 userid 읽기 */
  const getUserIdFromCookie = () => {
    const match = document.cookie.match(/userid=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
  };

  /** 🔥 localStorage에서도 한번 더 체크 (쿠키 없이 로컬로그인 할 경우 대비) */
  const getUserIdSafe = () => {
    const fromCookie = getUserIdFromCookie();
    if (fromCookie) return fromCookie;

    const fromLocal = localStorage.getItem("userid");
    if (fromLocal) return fromLocal;

    return null;
  };

  const submitFile = async () => {
    const file = fileInputRef.current?.files?.[0];
    if (!file) return alert("CSV 파일을 선택하세요.");

    // 검증
    if (!/\.csv$/i.test(file.name)) return alert("CSV 파일만 업로드 가능합니다.");
    if (file.size > 5 * 1024 * 1024)
      return alert("5MB 이하 파일만 업로드 가능합니다.");

    /** 🔥 userid 확보 */
    const userid = getUserIdSafe();
    if (!userid) {
      console.warn("userid 없음 → 제출 불가");
      return alert("로그인이 필요합니다. 다시 로그인 해주세요.");
    }

    try {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("userid", userid);

      await api.post(
        `/api/competitions/${competitionId}/submit`,
        formData,
        {
          withCredentials: true,
        }
      );

      alert(`"${file.name}" 제출 완료! 점수 계산 중...`);
      navigate("/leaderboard");

    } catch (error) {
      console.error(error);
      alert("제출 중 오류가 발생했습니다.");
    }
  };

  const Section = () => (
    <div className="grid">
      <article className="card">
        <span className="card-title">대회 정보</span>
        <h3>설명</h3>
        <p className="muted">
          주어진 이미지 데이터셋으로 고양이/강아지를 분류하는 모델을 학습하세요.
          Accuracy 기준으로 평가됩니다.
        </p>
        <ul style={{ marginLeft: 5 }}>
          <li>파일 형식: <code>submission.csv</code></li>
          <li>평가지표: Accuracy</li>
          <li>제출 마감: 2025-12-01</li>
        </ul>
      </article>

      <article className="card">
        <span className="card-title">데이터셋</span>
        <h3>데이터 다운로드</h3>
        <p className="muted">Train/Test 파일을 내려받아 학습하세요.</p>
        <a href="/data/train.csv" className="link" download>
          train.csv 다운로드
        </a>
        <a href="/data/test.csv" className="link" download style={{ marginLeft: 12 }}>
          test.csv 다운로드
        </a>
      </article>

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
    <section className="section-wrap">
      <div className="competition-title">
        <div>
          <h1>이미지 분류 챌린지</h1>
          <p>Transfer Learning으로 고양이·강아지를 분류하세요!</p>
        </div>
      </div>
      <Section />
    </section>
  );
};

export default Competition;
