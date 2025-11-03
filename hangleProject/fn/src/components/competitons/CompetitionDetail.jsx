// src/components/competitions/CompetitionDetail.jsx
import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';
// CompetitionDetail.jsx
import '../../styles/pages/CompetitionDetail.scss';

function CompetitionDetail() {
  const { id } = useParams();
  const [comp, setComp] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function fetchDetail() {
      setLoading(true);
      try {
        const res = await axios.get(`/api/competitions/${id}`);
        setComp(res.data);
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
      } finally {
        setLoading(false);
      }
    }
    fetchDetail();
  }, [id]);

  if (loading) return <div style={{ padding: 24 }}>불러오는 중...</div>;
  if (!comp) return <div style={{ padding: 24 }}>데이터가 없습니다.</div>;

  return (
    <div className="container comp-detail">
      <a className="back" href="/competitions">← 목록으로</a>

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

export default CompetitionDetail;
