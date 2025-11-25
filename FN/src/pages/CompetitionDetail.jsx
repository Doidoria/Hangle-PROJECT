import { useEffect, useMemo, useState, useRef } from "react";
import { useParams, Link } from "react-router-dom";
import api from "../api/axiosConfig";

import "../css/Competition.scss";
import "../css/CompetitionDetail.scss";
import { useNavigate } from "react-router-dom";

export default function CompetitionDetail() {

  const { id } = useParams();
  const [comp, setComp] = useState(null);
  const [state, setState] = useState({ loading: false, error: null });

  const [selectedFile, setSelectedFile] = useState(null);

  // 위치 계산 ref & state
  const submitBtnRef = useRef(null);
  const [submitOpen, setSubmitOpen] = useState(false);
  const [dropdownPos, setDropdownPos] = useState({ top: 0, left: 0 });

  const navigate = useNavigate();

  // --------------------------
  // 대회 상세 조회
  // --------------------------
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


  // --------------------------
  // 제출 드랍다운 열기/닫기 + 위치 계산
  // --------------------------
  const OFFSET_X = -280;   // 원하는 만큼 조절
  const OFFSET_Y = 5;   // 원하는 만큼 조절

  const toggleDropdown = () => {
    if (submitBtnRef.current) {
      const rect = submitBtnRef.current.getBoundingClientRect();

      setDropdownPos({
        top: rect.bottom + OFFSET_Y,
        left: rect.left + OFFSET_X
      });
    }

    setSubmitOpen(prev => !prev);
  };


  // --------------------------
  // 날짜 포맷
  // --------------------------
  const fmtDate = (value) => {
    if (!value) return "-";

    if (Array.isArray(value)) {
      const [y, m, d] = value;
      return `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
    }

    const str = String(value);

    if (/^\d{8,}/.test(str)) {
      const y = str.slice(0, 4);
      const m = str.slice(4, 6);
      const d = str.slice(6, 8);
      return `${y}-${m}-${d}`;
    }

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


  // --------------------------
  // 제출 파일 업로드
  // --------------------------
  const submitFile = async () => {

    if (!selectedFile) {
      alert("CSV 파일을 선택하세요.");
      return;
    }

    const isCsv = /\.csv$/i.test(selectedFile.name);
    const isUnder5MB = selectedFile.size <= 5 * 1024 * 1024;

    if (!isCsv) return alert("CSV 파일만 업로드 가능");
    if (!isUnder5MB) return alert("파일 크기는 5MB 이하만 가능");

    try {
      const formData = new FormData();
      formData.append("file", selectedFile);

      const userid = localStorage.getItem("userid") || "test_user";
      formData.append("userid", userid);

      await api.post(`/api/competitions/${id}/submit`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      alert(`"${selectedFile.name}" 제출 완료! 점수 계산 중입니다.`);
      setSelectedFile(null);

      // 제출 성공 -> 리더보드 페이지로 이동
      navigate("/leaderboard");

    } catch (e) {
      console.error("[제출 실패]", e);

      // 서버에서 응답한 메시지
      const msg = e.response?.data;

      if (!e.response) {
        // 서버가 꺼져 있거나, 네트워크 오류 시
        alert("서버와 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
        return;
      }

      // 중복 제출 메시지
      if (msg === "해당 대회는 이미 제출하셨습니다.") {
        alert("이미 제출하셨습니다.");
        return;
      }

      // 유저 없음
      if (msg === "INVALID_USER") {
        alert("로그인 정보가 유효하지 않습니다.");
        return;
      }

      // 대회 없음
      if (msg === "INVALID_COMPETITION") {
        alert("대회 정보를 찾을 수 없습니다.");
        return;
      }

      // 기타 메시지
      alert(msg || "제출 중 오류가 발생했습니다.");
    }
  };


  if (state.loading) return <div style={{ padding: 24 }}>불러오는 중...</div>;
  if (!comp) return <div style={{ padding: 24 }}>데이터가 없습니다.</div>;


  return (
    <div className="container comp-detail">

      {/* 상단바 */}
      <div className="top-bar">

        <Link className="back" to="/competitions/user">
          ← 목록으로
        </Link>

        <button
          ref={submitBtnRef}
          className="submit-toggle-btn"
          onClick={toggleDropdown}
        >
          제출하기 {submitOpen ? "▲" : "▼"}
        </button>

      </div>


      {/* 드랍다운 */}
      {submitOpen && (
        <div
          className="submit-dropdown"
          style={{
            top: dropdownPos.top,
            left: dropdownPos.left,
          }}
        >
          <div className="submit-row">

            <button
              className="file-btn"
              type="button"
              onClick={() => document.getElementById("detail-file").click()}
            >
              파일 선택
            </button>

            <input
              id="detail-file"
              type="file"
              accept=".csv,text/csv"
              style={{ display: "none" }}
              onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
            />

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


      {/* 에러 메시지 */}
      {state.error && (
        <div style={{ marginTop: 12, color: "#b91c1c" }}>{state.error}</div>
      )}


      {/* 카드 섹션 */}
      <div className="detail-cards">

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


        <article className="wide-card">
          <h3>대회 설명</h3>
          <div className="card-content">
            {comp.detail || "설명이 없습니다."}
          </div>
        </article>


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

            {/* 데이터셋 다운로드 */}
            <p>
              <strong>데이터셋:</strong>{" "}
              {comp.trainFilePath || comp.testFilePath ? (
                <>
                  <a
                    href={`${api.defaults.baseURL}/api/competitions/${id}/download/train`}
                    download
                    className="dataset-link"
                  >
                    train.csv
                  </a>
                  {"  |  "}
                  <a
                    href={`${api.defaults.baseURL}/api/competitions/${id}/download/test`}
                    download
                    className="dataset-link"
                  >
                    test.csv
                  </a>
                </>
              ) : (
                <span className="muted">없음</span>
              )}
            </p>

            {/* 규칙 URL */}
            <p>
              <strong>규칙:</strong>{" "}
              {comp.rulesUrl ? (
                <a
                  href={comp.rulesUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="rule-link"
                >
                  url 하이퍼링크
                </a>
              ) : (
                <span className="muted">없음</span>
              )}
            </p>

          </div>
        </article>



        <article className="wide-card">
          <h3>상세 설명</h3>
          <div className="card-content muted">(내용 없음)</div>
        </article>

      </div>
    </div>
  );
}
