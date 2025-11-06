// pages/CompetitionDetail.jsx
import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';

import '../css/competitionStyle/pages/CompetitionDetail.scss';


const API_BASE = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8095';

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
          description: '예시 설명입니다.',
          datasetUrl: '#',
          rulesUrl: '#',
        });
        setState({
          loading: false,
          error: e.response?.status === 404 ? '존재하지 않는 대회입니다.' : (e.message || '요청 실패'),
        });
      }
    })();
  }, [id]);

  if (state.loading) return <div style={{ padding: 24 }}>불러오는 중...</div>;
  if (!comp) return <div style={{ padding: 24 }}>데이터가 없습니다.</div>;

  return (
    <div className="container comp-detail">
      <Link className="back" to="/competitions">← 목록으로</Link>

      {state.error && <div style={{ marginTop: 12, color: '#b91c1c' }}>{state.error}</div>}

      <section className="hero">
        <h1>{comp.title}</h1>
        <div className="meta">상태: {comp.status} | 기간: {comp.startAt} ~ {comp.endAt}</div>
        {comp.prize && <div className="prize">상금: {comp.prize}</div>}
        <div className="links">
          {comp.datasetUrl && <a href={comp.datasetUrl} target="_blank" rel="noreferrer" className="btn">데이터셋</a>}
          {comp.rulesUrl && <a href={comp.rulesUrl} target="_blank" rel="noreferrer" className="btn">규칙</a>}
        </div>
      </section>

      <section className="desc">{comp.description || '설명이 없습니다.'}</section>
    </div>
  );
}
