// src/pages/Competition.jsx
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import '../css/competitionStyle/pages/CompetitionList.scss';
import { useAuth } from "../api/AuthContext";

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8090';

export default function Competition() {
  const { role } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();

  const status = searchParams.get('status') || 'OPEN';
  const keyword = searchParams.get('keyword') || '';
  const page = Number(searchParams.get('page') || 0);
  const size = Number(searchParams.get('size') || 12);
  const sort = searchParams.get('sort') || 'createdAt,desc';

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await axios.get(`${API_BASE}/api/competitions`, {
          params: { status, keyword, page, size, sort },
          withCredentials: true
        });
        const data = res.data ?? [];
        const list = Array.isArray(data) ? data : (data.content ?? []);
        setItems(list);
      } catch {
        setItems([]);
      } finally {
        setLoading(false);
      }
    })();
  }, [status, keyword, page, size, sort]);

  const onSearch = (e) => {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    setSearchParams({ status, keyword: form.get('keyword') || '', page: 0, size, sort });
  };

  return (
    <section className="section-wrap">
      <div className="competiton-title">
        <div>
          <h1>대회 목록</h1>
          <p>진행 중인 대회에 참여해보세요.</p>
        </div>
        <div style={{ display:'flex', gap:8, alignItems:'center' }}>
          <form onSubmit={onSearch} style={{ display:'flex', gap:8 }}>
            <input name="keyword" placeholder="검색어" defaultValue={keyword} />
            <button className="btn" type="submit">검색</button>
          </form>
          {role === 'MANAGER' && (
            <Link to="/competitions/new" className="btn" style={{ marginLeft: 8 }}>
              대회 만들기
            </Link>
          )}
        </div>
      </div>

      <div className="grid">
        {loading ? (
          <div className="muted">불러오는 중…</div>
        ) : items.length === 0 ? (
          <div className="muted">표시할 대회가 없습니다.</div>
        ) : (
          items.map((c) => (
            <Link key={c.id} to={`/competitions/${c.id}`} style={{ textDecoration:'none', color:'inherit' }}>
              <article className="card">
                <span className="card-title">추천 대회</span>
                <h3>{c.title ?? '제목 없음'}</h3>
                <p className="muted">{c.summary || '설명 없음'}</p>
                <div className="muted">기간: {c.startAt?.slice(0,10)} ~ {c.endAt?.slice(0,10)}</div>
              </article>
            </Link>
          ))
        )}
      </div>
    </section>
  );
}
