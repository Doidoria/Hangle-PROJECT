// pages/CompetitionDetail.jsx
import { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';
import Layout from './Layout.jsx';

import '../css/competitionStyle/pages/CompetitionDetail.scss';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8090';

export default function CompetitionDetail() {
  const { id } = useParams();
  const [comp, setComp] = useState(null);
  const [state, setState] = useState({ loading: false, error: null });

  useEffect(() => {
    (async () => {
      setState({ loading: true, error: null });
      try {
        const res = await axios.get(`${API_BASE}/api/competitions/${id}`);
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

  if (state.loading) return <div style={{ padding: 24 }}>불러오는 중...</div>;
  if (!comp) return <div style={{ padding: 24 }}>데이터가 없습니다.</div>;

  return (
      <div className="container comp-detail">
        <Link className="back" to="/competitions">
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

        {/* ✅ 세로 배치 / 가로로 넓은 카드 */}
        <div className="detail-cards">
          <article className="wide-card">
            <h3>📅 진행 정보</h3>
            <div className="card-content">
              <p><strong>상태:</strong> {comp.status}</p>
              <p><strong>기간:</strong> {fmtDate(comp.startAt)} ~ {fmtDate(comp.endAt)}</p>
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
              <p><strong>제목:</strong> {comp.title}</p>
              <p><strong>요약:</strong> {comp.summary || '—'}</p>
              <p><strong>ID:</strong> {comp.id}</p>
            </div>
          </article>
        </div>

        {/* 상세 설명 */}
        <section className="desc">
          <h3>📝 대회 설명</h3>
          <p>{comp.description || '설명이 없습니다.'}</p>
        </section>
      </div>
  );
}
