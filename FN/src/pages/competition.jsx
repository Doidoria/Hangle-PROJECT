import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../css/competition.scss";
import api from "../api/axiosConfig";

const Competition = () => {
  const navigate = useNavigate();

  const [competitions, setCompetitions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");
  const [files, setFiles] = useState({}); // 각 대회별 파일 저장

  // ============================
  // 대회 목록 불러오기
  // ============================
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

        if (!list.length) setErrorMsg("현재 진행 중인 대회가 없습니다.");
        else setCompetitions(list);
      } catch (e) {
        console.error("[대회 조회 오류]", e);
        setErrorMsg("대회 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchCompetitions();
  }, []);

  // ============================
  // 파일 선택 핸들러
  // ============================
  const handleFileChange = (competitionId, e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setFiles((prev) => ({
      ...prev,
      [competitionId]: file,
    }));
  };

  // ============================
  // 제출 버튼 클릭 시
  // ============================
  const submitFile = async (competitionId) => {
  const file = files[competitionId];
  if (!file) {
    alert("CSV 파일을 선택하세요.");
    return;
  }

  // 유효성 검증
  if (!/\.csv$/i.test(file.name)) {
    alert("CSV 파일만 업로드할 수 있습니다.");
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    alert("파일 크기는 5MB 이하만 허용됩니다.");
    return;
  }

  // ★ 서버로 전송할 FormData
  const formData = new FormData();
  formData.append("file", file);

  try {
    const res = await api.post(
      `/api/competitions/${competitionId}/submit`,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );

    alert(`제출 완료: ${file.name}\n${res.data.message}`);
  } catch (err) {
    console.error(err);
    alert("제출 중 오류 발생");
  }
};

  // ============================
  // 화면 렌더링
  // ============================
  const Section = () => {
    if (loading)
      return (
        <div className="competition-section">
          <div className="grid competition-list">
            <article className="card">
              <h3>대회 정보를 불러오는 중입니다...</h3>
            </article>
          </div>
        </div>
      );

    if (errorMsg)
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

    return (
      <div className="competition-section">
        <div className="grid competition-list">
          {competitions.map((competition) => {
            const { id, title, purpose, startAt, endAt, status, prizeTotal, participantCount } = competition;

            return (
              <article key={id} className="card competition-card">
                {/* 카드 상단 */}
                <div className="card-top">
                  <span className="card-title">대회 카드</span>
                  <h3>{title || "대회 제목 미정"}</h3>
                </div>

                <div className="card-main">
                  {/* 1. 대회 정보 */}
                  <div className="card-col card-info">
                    <h4>대회 정보</h4>
                    <p className="muted">{purpose || "대회 설명이 없습니다."}</p>
                    <ul className="meta-list">
                      <li>
                        상태: <code>{status}</code>
                      </li>
                      <li>
                        기간: {startAt} ~ {endAt}
                      </li>
                      <li>참가자 수: {participantCount ?? 0}명</li>
                      {prizeTotal && <li>총 상금: {prizeTotal.toLocaleString()}원</li>}
                    </ul>

                    <button className="btn" onClick={() => navigate(`/competitions/${id}`)}>
                      자세히 보기
                    </button>
                  </div>

                  {/* 2. 데이터 다운로드 */}
                  <div className="card-col card-download">
                    <h4>데이터 다운로드</h4>
                    <p className="muted small">
                      Train / Test 파일을 내려받아 모델을 학습하세요.
                    </p>

                    <div className="download-links">
                      <a href="/data/train.csv" className="link" download>
                        train.csv 다운로드
                      </a>
                      <a href="/data/test.csv" className="link" download>
                        test.csv 다운로드
                      </a>
                    </div>
                  </div>

                  {/* 3. 결과 제출 */}
                  <div className="card-col card-submit">
                    <h4>결과 제출</h4>
                    <p className="muted small">
                      예측 결과 CSV를 업로드하면 점수가 자동 계산된다고 가정합니다.
                    </p>

                    {/* 숨겨진 input */}
                    <input
                      id={`file-input-${id}`}
                      type="file"
                      accept=".csv,text/csv"
                      onChange={(e) => handleFileChange(id, e)}
                      style={{ display: "none" }}
                    />

                    {/* 파일 선택 버튼 (항상 버튼처럼 보임) */}
                    <label
                      htmlFor={`file-input-${id}`}
                      style={{
                        cursor: "pointer",
                        backgroundColor: "#10B981",
                        color: "#fff",
                        padding: "5px 10px",
                        borderRadius: "6px",
                        fontSize: "14px",
                        fontWeight: "600",
                        display: "inline-block",
                        marginRight: "12px",
                      }}
                    >
                      파일 선택
                    </label>

                    {/* 파일명 표시 (한 줄, 버튼 옆) */}
                    <span
                      style={{
                        display: "inline-block",
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        maxWidth: "180px",
                        verticalAlign: "middle",
                        marginRight: "12px",
                      }}
                    >
                      선택된 파일: <strong>{files[id]?.name || "없음"}</strong>
                    </span>

                    {/* 제출 버튼 (항상 아래 줄로) */}
                    <div>
                      <button
                        className="btn"
                        onClick={() => submitFile(id)}
                        style={{ marginTop: "8px" }}
                      >
                        제출하기
                      </button>
                    </div>
                  </div>
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
          <h1>이미지 분류 챌린지</h1>
          <p>Transfer Learning으로 고양이·강아지를 분류하세요!</p>
        </div>
      </div>

      <Section />
    </section>
  );
};

export default Competition;
