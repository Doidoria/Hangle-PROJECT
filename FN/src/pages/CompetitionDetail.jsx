import { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axiosConfig';

import '../css/competitionStyle/pages/CompetitionDetail.scss';

export default function CompetitionDetail() {
  const { id } = useParams();
  const [comp, setComp] = useState(null);
  const [state, setState] = useState({ loading: false, error: null });

  // ★ 추가됨: 파일 선택 상태
  const [selectedFile, setSelectedFile] = useState(null); // 선택된 csv 파일
  const [fileName, setFileName] = useState(""); // 화면에 표시될 파일명

  useEffect(() => {
    (async () => {
      setState({ loading: true, error: null });
      try {
        const res = await api.get(`/api/competitions/${id}`);
        setComp(res.data);
        setState({ loading: false, error: null });
      } catch (e) {
        setComp({
          id,
          title: '예시 대회',
          status: 'OPEN',
          startAt: '2025-11-01',
          endAt: '2025-12-01',
          prize: '총상금 100만원',
          summary: '예시 요약입니다.',
          description: '예시 설명입니다.',
          datasetUrl: '#',
          rulesUrl: '#',
        });
        setState({
          loading: false,
          error:
            e.response?.status === 404
              ? '존재하지 않는 대회입니다.'
              : e.message || '요청 실패',
        });
      }
    })();
  }, [id]);

  const fmtDate = (d) => (typeof d === 'string' ? d.slice(0, 10) : d);

  const daysLeft = useMemo(() => {
    if (!comp?.endAt) return null;
    const end = new Date(comp.endAt);
    const diff = Math.ceil((end - new Date()) / (1000 * 60 * 60 * 24));
    return diff;
  }, [comp?.endAt]);

  // ★ 추가됨: 파일 선택 핸들러
  const handleFileChange = (e) => {
  const file = e.target.files && e.target.files[0];
  if (file) {
    console.log("선택된 파일:", file.name);   // 콘솔 확인용
    setSelectedFile(file);
    setFileName(file.name);
  } else {
    setSelectedFile(null);
    setFileName("");
  }
};

  // ★ 추가됨: 제출하기 버튼 동작
  const handleSubmitCSV = async () => {
    if (!selectedFile) {
      alert("CSV 파일을 선택해주세요.");
      return;
    }

    try {
      const formData = new FormData();
      formData.append("file", selectedFile);

      const res = await api.post(`/api/competitions/${id}/submit`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      alert(`"${fileName}" 제출 완료! (대회 ID: ${id})`);
    } catch (err) {
      console.error(err);
      alert("CSV 제출 중 오류 발생");
    }
  };

  if (state.loading) return <div style={{ padding: 24 }}>불러오는 중...</div>;
  if (!comp) return <div style={{ padding: 24 }}>데이터가 없습니다.</div>;

  return (
    <div className="container comp-detail">
      <Link className="back" to="/competitions/user">
        ← 목록으로
      </Link>
      {state.error && (
        <div style={{ marginTop: 12, color: '#b91c1c' }}>{state.error}</div>
      )}

      {/* 상단 정보 */}
      <section className="hero">
        <h1>{comp.title}</h1>
        <div className="meta">
          상태:&nbsp;
          <span className="badge">{comp.status}</span> | 기간:{' '}
          {fmtDate(comp.startAt)} ~ {fmtDate(comp.endAt)}
        </div>
        {comp.summary && (
          <p className="muted" style={{ marginTop: 6 }}>
            {comp.summary}
          </p>
        )}
        {comp.prize && <div className="prize">상금: {comp.prize}</div>}

        <div className="links">
          {comp.datasetUrl && (
            <a
              href={comp.datasetUrl}
              target="_blank"
              rel="noreferrer"
              className="btn"
            >
              데이터셋
            </a>
          )}
          {comp.rulesUrl && (
            <a
              href={comp.rulesUrl}
              target="_blank"
              rel="noreferrer"
              className="btn"
            >
              규칙
            </a>
          )}
        </div>
      </section>

      {/* 세부 카드들 */}
      <div className="detail-cards">
        <article className="wide-card">
          <h3>📅 진행 정보</h3>
          <div className="card-content">
            <p>
              <strong>상태:</strong> {comp.status}
            </p>
            <p>
              <strong>기간:</strong> {fmtDate(comp.startAt)} ~{' '}
              {fmtDate(comp.endAt)}
            </p>
            <p>
              <strong>남은 기간:</strong>{' '}
              {daysLeft === null
                ? '-'
                : daysLeft >= 0
                ? `${daysLeft}일 남음`
                : `마감 (${Math.abs(daysLeft)}일 경과)`}
            </p>
          </div>
        </article>

        <article className="wide-card">
          <h3>💰 보상 정보</h3>
          <div className="card-content">
            <p>
              <strong>상금:</strong> {comp.prize || '표기된 상금 없음'}
            </p>
            <p className="muted">
              우승자 및 상위권 참가자에게 제공되는 보상 정보를 표시하세요.
            </p>
          </div>
        </article>

        <article className="wide-card">
          <h3>🔗 참고 링크</h3>
          <div className="card-content">
            <p>
              데이터셋:{' '}
              {comp.datasetUrl ? (
                <a href={comp.datasetUrl} target="_blank" rel="noreferrer">
                  열기
                </a>
              ) : (
                <span className="muted">없음</span>
              )}
            </p>
            <p>
              규칙:{' '}
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

        <article className="wide-card">
          <h3>🧾 기본 정보</h3>
          <div className="card-content">
            <p>
              <strong>제목:</strong> {comp.title}
            </p>
            <p>
              <strong>요약:</strong> {comp.summary || '—'}
            </p>
            <p>
              <strong>ID:</strong> {comp.id}
            </p>
          </div>
        </article>
      </div>

      {/* 상세 설명 */}
      <section className="desc">
        <h3>📝 대회 설명</h3>
        <p>{comp.description || '설명이 없습니다.'}</p>
      </section>

            {/* ★ CSV 제출 UI (파일명 표시 버전) */}
      <section className="csv-submit" style={{ marginTop: 40 }}>
        <h3>📤 결과 제출</h3>
        <p className="muted">
          예측 결과 CSV 파일을 업로드하세요. 제출 시 점수가 자동 계산됩니다.
        </p>

        <div style={{ marginTop: 12, display: 'flex', alignItems: 'center', gap: 12 }}>
          {/* 기본 input: 브라우저가 자동으로 파일명도 보여줌 */}
          <input
            type="file"
            accept=".csv"
            onChange={handleFileChange}   // ★ 여기 꼭 연결
          />

          {/* 옆에 내가 제어하는 텍스트도 같이 표시 */}
          <span>
            {fileName || "선택된 파일 없음"}
          </span>
        </div>

        <button
          className="btn"
          style={{ marginTop: 12 }}
          onClick={handleSubmitCSV}
        >
          제출하기
        </button>
      </section>
    </div>
  );
}
