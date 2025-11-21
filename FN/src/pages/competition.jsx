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

      // 제출 후 해당 대회 파일 선택 상태 초기화 (선택사항)
      setFiles((prev) => ({
        ...prev,
        [competitionId]: undefined,
      }));
    } catch (e) {
      console.error("[제출 실패]", e);
      alert("제출 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
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
                      {/* <a
                        href={`${API_BASE_URL}/api/competitions/${id}/dataset/train`}
                        className="link"
                      >
                        train.csv 다운로드
                      </a>
                      <a
                        href={`${API_BASE_URL}/api/competitions/${id}/dataset/test`}
                        className="link"
                      >
                        test.csv 다운로드
                      </a> */}
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
                      : "선택된 파일 없음"}
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
