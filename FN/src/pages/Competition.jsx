import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axiosConfig";
import "../css/Competition.scss";

const API_BASE_URL = api.defaults.baseURL || "";

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

  const fmtDate = (value) => {
    if (!value) return "-";

    // 배열 형태 [yyyy, mm, dd]
    if (Array.isArray(value)) {
      const [y, m, d] = value;
      return `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
    }

    const str = String(value);

    // yyyyMMdd 또는 yyyyMMddHHmm
    if (/^\d{8,}/.test(str)) {
      const y = str.slice(0, 4);
      const m = str.slice(4, 6);
      const d = str.slice(6, 8);
      return `${y}-${m}-${d}`;
    }

    // ISO "2025-11-18T00:00:00"
    if (str.includes("-")) {
      const date = new Date(str);
      if (!isNaN(date)) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, "0");
        const d = String(date.getDate()).padStart(2, "0");
        return `${y}-${m}-${d}`;
      }
    }

    return "-";
  };

  const handleFileChange = (competitionId, e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setFiles((prev) => ({
      ...prev,
      [competitionId]: file,
    }));
  };

  const downloadFile = async (saveId, fileName) => {
    try {
      const res = await api.get(`/api/competitions/csv/${saveId}/download`, {
        responseType: "blob",
      });

      // Blob 생성
      const blob = new Blob([res.data], { type: "text/csv" });
      const url = window.URL.createObjectURL(blob);

      // 가짜 링크 생성
      const a = document.createElement("a");
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      a.remove();

      // URL 해제
      window.URL.revokeObjectURL(url);

    } catch (e) {
      console.error("[파일 다운로드 오류]", e);
      alert("파일 다운로드 중 문제가 발생했습니다.");
    }
  };

  const submitFile = async (competitionId) => {
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

    try {
      const formData = new FormData();
      formData.append("file", file);

      // TODO: 실제 로그인 유저 id로 교체
      const userid = localStorage.getItem("userid") || "test_user";
      formData.append("userid", userid);

      await api.post(
        `/api/competitions/${competitionId}/submit`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      alert(`"${file.name}" 제출 완료! 점수 계산 중입니다.`);

      // window.location.reload(); // 페이지 새로고침 (제출 시 참가자 바로 반영을 위해 사용)

      // 제출 성공 시 리더보드 페이지로 이동
      navigate("/leaderboard");

      // 제출 후 해당 대회 파일 선택 상태 초기화 (선택사항)
      setFiles((prev) => ({
        ...prev,
        [competitionId]: undefined,
      }));
    } catch (e) {
      console.error("[제출 실패]", e);

      const msg = e.response?.data;

      // 통신 실패 (서버 꺼짐 등)
      if (!e.response) {
        alert("서버와 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
        return;
      }

      // 중복 제출
      if (msg === "횟수를 모두 소진") {
        alert("오늘 제출 가능 횟수를 모두 소진하였습니다.(일 n회) 내일 다시 시도 또는 관리자에게 문의해주세요.");
        return;
      }

      // 사용자 오류
      if (msg === "INVALID_USER") {
        alert("로그인 정보가 유효하지 않습니다.");
        return;
      }

      // 대회 오류
      if (msg === "INVALID_COMPETITION") {
        alert("대회 정보를 찾을 수 없습니다.");
        return;
      }

      // 기타
      alert(msg || "제출 중 오류가 발생했습니다.");
    }
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

            // 대회 상세 API 결과를 competition 변수에 넣었다고 가정
            const trainId = competition.trainDatasetSaveId;
            const testId = competition.testDatasetSaveId;

            const selectedFile = files[id];

            return (
              <article key={id} className="card competition-card">
                {/* 상단: 왼쪽 제목, 오른쪽 자세히 보기 */}
                <div className="card-top">
                  <div className="card-top-left">
                    <span className="card-title">대회 카드</span>
                    <h3>{title || "대회 제목 미정"}</h3>
                  </div>
                  <button
                    type="button"
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
                        기간: {fmtDate(startAt)} ~ {fmtDate(endAt)}
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
                      <button
                        className="link"
                        onClick={() => downloadFile(trainId, "train.csv")}
                      >
                        train.csv 다운로드
                      </button>

                      <button
                        className="link"
                        onClick={() => downloadFile(testId, "test.csv")}
                      >
                        test.csv 다운로드
                      </button>
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

                {/* 하단: 파일 선택 + 제출하기 */}
                <div className="card-bottom">
                  {/* 실제 파일 인풋은 숨김 */}
                  <input
                    id={`csv-input-${id}`}
                    type="file"
                    accept=".csv,text/csv"
                    style={{ display: "none" }}
                    aria-label={`대회 ${id} 예측 결과 CSV 업로드`}
                    onChange={(e) => handleFileChange(id, e)}
                  />

                  {/* 파일 선택 버튼 (label이 input을 대신 클릭) */}
                  <label
                    htmlFor={`csv-input-${id}`}
                    className="btn btn-outline"
                    style={{ marginRight: "8px", cursor: "pointer" }}
                  >
                    파일 선택
                  </label>

                  {/* 선택된 파일명 표시 */}
                  <span className="file-name muted" style={{ marginRight: "auto" }}>
                    {selectedFile
                      ? `선택된 파일: ${selectedFile.name}`
                      : '제출 CSV 컬럼명은 반드시 ( label )해주세요.'}
                  </span>

                  {/* 제출 버튼 */}
                  <button
                    type="button"
                    className="btn"
                    onClick={() => submitFile(id)}
                    disabled={!selectedFile}
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
