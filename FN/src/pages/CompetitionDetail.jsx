import { useEffect, useMemo, useState } from "react";
import { useParams, Link } from "react-router-dom";
import api from "../api/axiosConfig";

import "../css/Competition.scss";
import "../css/CompetitionDetail.scss";

export default function CompetitionDetail() {
  const { id } = useParams(); // competitionId
  const [comp, setComp] = useState(null);
  const [state, setState] = useState({ loading: false, error: null });
  const [selectedFile, setSelectedFile] = useState(null);
  const [submitOpen, setSubmitOpen] = useState(false);

  // 대회 상세 조회
  useEffect(() => {
    (async () => {
      setState({ loading: true, error: null });
      try {
        const res = await api.get(`/api/competitions/${id}`);
        setComp(res.data);
        setState({ loading: false, error: null });
      } catch (e) {
        console.error("[대회 상세 조회 오류]", e);
        setComp({
          id,
          title: "예시 대회",
          status: "OPEN",
          startAt: "2025-11-01",
          endAt: "2025-12-01",
          prizeTotal: 1000000,
          purpose: "예시 요약입니다.",
          detail: "예시 설명입니다.",
          datasetUrl: "#",
          rulesUrl: "#",
        });
        setState({
          loading: false,
          error:
            e.response?.status === 404
              ? "존재하지 않는 대회입니다."
              : e.message || "요청 실패",
        });
      }
    })();
  }, [id]);

  // 날짜 포맷
  const fmtDate = (value) => {
    if (!value) return "-";

    if (Array.isArray(value)) {
      const [y, m, d] = value;
      return `${y}.${String(m).padStart(2, "0")}.${String(d).padStart(2, "0")}`;
    }

    const str = String(value);

    if (/^\d{8,}/.test(str)) {
      const y = str.slice(0, 4);
      const m = str.slice(4, 6);
      const d = str.slice(6, 8);
      return `${y}.${m}.${d}`;
    }

    if (str.includes("-")) {
      const date = new Date(str);
      if (!isNaN(date)) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, "0");
        const d = String(date.getDate()).padStart(2, "0");
        return `${y}.${m}.${d}`;
      }
    }

    return "-";
  };

  const daysLeft = useMemo(() => {
    if (!comp?.endAt) return null;
    const end = new Date(comp.endAt);
    return Math.ceil((end - new Date()) / (1000 * 60 * 60 * 24));
  }, [comp?.endAt]);

  // 🔹 Competition.jsx 의 submitFile 로직을 Detail에 맞게 이식
  const submitFile = async () => {
    const file = selectedFile;
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

      const userid = localStorage.getItem("userid") || "test_user";
      formData.append("userid", userid);

      await api.post(`/api/competitions/${id}/submit`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      alert(`"${file.name}" 제출 완료! 점수 계산 중입니다.`);

      // 제출 후 파일 상태 초기화
      setSelectedFile(null);
    } catch (e) {
      console.error("[제출 실패]", e);
      alert("제출 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
  };

  if (state.loading) return <div style={{ padding: 24 }}>불러오는 중...</div>;
  if (!comp) return <div style={{ padding: 24 }}>데이터가 없습니다.</div>;

  return (
    <div className="container comp-detail">
      {/* 상단 바: 좌측 ← 목록으로 / 우측 제출하기 */}
      <div className="top-bar">
        <Link className="back" to="/competitions/user">
          ← 목록으로
        </Link>

        <button
          className="submit-toggle-btn"
          onClick={() => setSubmitOpen((prev) => !prev)}
        >
          제출하기 {submitOpen ? "▲" : "▼"}
        </button>
      </div>

      {/* 드롭다운: 상단바 기준 absolute로 겹쳐서 표시 */}
      {submitOpen && (
        <div className="submit-dropdown">
          <div className="submit-row">
            {/* 파일 선택 버튼 */}
            <button
              className="file-btn"
              onClick={() => document.getElementById("detail-file-input").click()}
              type="button"
            >
              파일 선택
            </button>

            {/* 숨겨진 실제 input */}
            <input
              id="detail-file-input"
              type="file"
              accept=".csv,text/csv"
              style={{ display: "none" }}
              onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
            />

            {/* 선택된 파일명 */}
            <div className="selected-file">
              {selectedFile ? selectedFile.name : "선택된 파일이 없습니다."}
            </div>
            <button
              type="button"
              className="btn"
              onClick={submitFile}
              disabled={!selectedFile}
            >
              제출하기
            </button>
          </div>
        </div>
      )}

      {state.error && (
        <div style={{ marginTop: 12, color: "#b91c1c" }}>{state.error}</div>
      )}

      {/* 카드 영역 */}
      <div className="detail-cards">
        {/* 1. 대회 기본 정보 */}
        <article className="wide-card">
          <h3>{comp.title}</h3>
          <div className="card-content">
            <p>
              <strong>상태:</strong> {comp.status} {" | "}
              <strong>기간:</strong> {fmtDate(comp.startAt)} ~ {fmtDate(comp.endAt)}
            </p>
            <p>
              <strong>요약:</strong> {comp.purpose || "—"}
            </p>
          </div>
        </article>

        {/* 2. 대회 설명 */}
        <article className="wide-card">
          <h3>대회 설명</h3>
          <div className="card-content">
            {comp.detail || "설명이 없습니다."}
          </div>
        </article>

        {/* 3. 보상 정보 */}
        <article className="wide-card">
          <h3>보상 정보</h3>
          <div className="card-content">
            <p>
              <strong>상금:</strong>{" "}
              {comp.prizeTotal
                ? `${comp.prizeTotal.toLocaleString()}원`
                : "표기된 상금 없음"}
            </p>
          </div>
        </article>

        {/* 4. 참고 링크 */}
        <article className="wide-card">
          <h3>참고 링크</h3>
          <div className="card-content">
            <p>
              데이터셋:{" "}
              {comp.datasetUrl ? (
                <a href={comp.datasetUrl} target="_blank" rel="noreferrer">
                  열기
                </a>
              ) : (
                <span className="muted">없음</span>
              )}
            </p>
            <p>
              규칙:{" "}
              {comp.rulesUrl ? (
                <a href={comp.rulesUrl} target="_blank" rel="noreferrer">
                  열기
                </a>
              ) : (
                <span className="muted">없음</span>
              )}
            </p>
          </div>
        </article>

        {/* 5. 상세 설명 */}
        <article className="wide-card">
          <h3>상세 설명</h3>
          <div className="card-content muted">(내용 없음)</div>
        </article>
      </div>
    </div>
  );
}
