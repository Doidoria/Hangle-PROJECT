import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "../css/Competition.scss";

const Competition = () => {
  const navigate = useNavigate();

  const [competitions, setCompetitions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");
  const [files, setFiles] = useState({}); // 각 대회별 업로드 파일

  useEffect(() => {
    const fetchCompetitions = async () => {
      try {
        const res = await api.get("/api/competitions", {
          params: { status: "OPEN", page: 0, size: 12 },
        });

        const data = res.data;
        let list = [];

        if (Array.isArray(data)) list = data;
        else if (Array.isArray(data?.content)) list = data.content;
        else if (Array.isArray(data?.items)) list = data.items;

        if (!list.length) {
          setErrorMsg("현재 진행 중인 대회가 없습니다.");
        } else {
          setCompetitions(list);
        }
      } catch (e) {
        console.error("[대회 조회 오류]", e);
        setErrorMsg("대회 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchCompetitions();
  }, []);

  const handleFileChange = (competitionId, e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setFiles((prev) => ({
      ...prev,
      [competitionId]: file,
    }));
  };

  const submitFile = (competitionId) => {
    const file = files[competitionId];
    if (!file) {
      alert("CSV 파일을 선택하세요.");
      return;
    }

    const isCsv = /\.csv$/i.test(file.name);
    const isUnder5MB = file.size <= 5 * 1024 * 1024;
    if (!isCsv) {
      alert("CSV 파일만 업로드할 수 있습니다.");
      return;
    }
    if (!isUnder5MB) {
      alert("파일 크기는 5MB 이하만 허용됩니다.");
      return;
    }

    alert(
      `"${file.name}" 제출 완료! (대회 ID: ${competitionId})\n점수 계산 중이라고 가정합니다.`
    );
    // 실제 구현 시: FormData 업로드 후 응답 보고 이동
    // setTimeout(() => navigate("/leaderboard"), 800);
  };

  const Section = () => {
    if (loading) {
      return (
        <div className="competition-section">
          <div className="grid competition-list">
            <article className="card">
              <h3>대회 정보를 불러오는 중입니다...</h3>
            </article>
          </div>
        </div>
      );
    }

    if (errorMsg) {
      return (
        <div className="competition-section">
          <div className="grid competition-list">
            <article className="card">
              <h3>대회 정보</h3>
              <p className="muted">{errorMsg}</p>
            </article>
          </div>
        </div>
      );
    }

    return (
      <div className="competition-section">
        <div className="grid competition-list">
          {competitions.map((competition) => {
            const {
              id,
              title,
              purpose,
              startAt,
              endAt,
              status,
              prizeTotal,
              participantCount,
            } = competition;

            return (
              <article key={id} className="card competition-card">
                {/* 상단: 왼쪽 제목, 오른쪽 자세히 보기 */}
                <div className="card-top">
                  <div className="card-top-left">
                    <span className="card-title">대회 카드</span>
                    <h3>{title || "대회 제목 미정"}</h3>
                  </div>
                  <button
                    className="btn btn-ghost"
                    onClick={() => navigate(`/competitions/${id}`)}
                  >
                    자세히 보기
                  </button>
                </div>

                {/* 가운데: 3등분 레이아웃 */}
                <div className="card-main">
                  {/* 1) 대회 정보 */}
                  <div className="card-col card-info">
                    <h4>대회 정보</h4>
                    <p className="muted">
                      {purpose || "대회 설명이 없습니다."}
                    </p>
                    <ul className="meta-list">
                      <li>
                        상태: <code>{status}</code>
                      </li>
                      <li>
                        기간: {startAt} ~ {endAt}
                      </li>
                      <li>
                        참가자 수: {participantCount ?? 0}명
                      </li>
                      {prizeTotal && (
                        <li>총 상금: {prizeTotal.toLocaleString()}원</li>
                      )}
                    </ul>
                  </div>

                  {/* 2) 데이터 다운로드 */}
                  <div className="card-col card-download">
                    <h4>데이터 다운로드</h4>
                    <p className="muted small">
                      Train / Test 파일을 내려받아 모델을 학습하세요.
                    </p>
                    <div className="download-links">
                      <a href="/data/train.csv" className="link" download>
                        train.csv 다운로드
                      </a>
                      <a
                        href="/data/test.csv"
                        className="link"
                        download
                      >
                        test.csv 다운로드
                      </a>
                    </div>
                  </div>

                  {/* 3) 결과 제출 (설명만) */}
                  <div className="card-col card-submit">
                    <h4>결과 제출</h4>
                    <p className="muted small">
                      예측 결과 CSV를 업로드하면 점수가 자동 계산된다고
                      가정합니다.
                    </p>
                  </div>
                </div>

                {/* 하단: 파일 선택 + 제출하기 → 오른쪽 하단 정렬 */}
                <div className="card-bottom">
                  <input
                    type="file"
                    accept=".csv,text/csv"
                    aria-label={`대회 ${id} 예측 결과 CSV 업로드`}
                    onChange={(e) => handleFileChange(id, e)}
                  />
                  <button
                    className="btn"
                    onClick={() => submitFile(id)}
                  >
                    제출하기
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      </div>
    );
  };

  return (
    <section className="section-wrap">
      <div className="competition-title">
        <div>
          <h1>대회 참여</h1>
          <p>다양한 대회에 참여해보세요!</p>
        </div>
      </div>
      <Section />
    </section>
  );
};

export default Competition;
