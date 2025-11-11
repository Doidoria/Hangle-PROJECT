// pages/CompetitionList.jsx (경로 네 프로젝트에 맞게)
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import axios from 'axios';

import '../css/competitionStyle/pages/CompetitionList.scss';
import Layout from './Layout';

const API_BASE = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8090';

export default function CompetitionList() {
  const [items, setItems] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 12, total: 0 });
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();

  const status = searchParams.get('status') || 'OPEN';
  const keyword = searchParams.get('keyword') || '';
  const page = Number(searchParams.get('page') || 0);
  const size = Number(searchParams.get('size') || 12);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await axios.get(`${API_BASE}/api/competitions`, { params: { status, keyword, page, size } });
        const data = res.data ?? [];
        const list = Array.isArray(data) ? data : (data.content ?? []);
        setItems(list);
        setPageInfo({
          page: data.page ?? page,
          size: data.size ?? size,
          total: data.totalElements ?? (Array.isArray(data) ? data.length : 0),
        });
      } catch {
        // 개발용 더미
        setItems([
          { id: 1, title: '예시 대회 1', summary: '간단 소개', status: 'OPEN', startAt: '2025-11-01', endAt: '2025-12-01' },
          { id: 2, title: '예시 대회 2', summary: '간단 소개', status: 'OPEN', startAt: '2025-11-10', endAt: '2025-12-15' },
        ]);
        setPageInfo({ page: 0, size: 12, total: 2 });
      } finally {
        setLoading(false);
      }
    })();
  }, [status, keyword, page, size]);

  const onSearch = (e) => {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    setSearchParams({ status, keyword: form.get('keyword') || '', page: 0, size });
  };

  const changeStatus = (next) => setSearchParams({ status: next, keyword, page: 0, size });
  const movePage = (nextPage) => setSearchParams({ status, keyword, page: nextPage, size });
  const totalPages = Math.max(1, Math.ceil(pageInfo.total / pageInfo.size));

  return (
    <Layout>
      <select onChange={(e) => setSearchParams({ sort: e.target.value })}>
        <option value="createdAt,desc">최신순</option>
        <option value="startAt,asc">시작일 오름차순</option>
        <option value="endAt,asc">마감일 오름차순</option>
      </select>
      <div className="container comp-list">
        <div className="header">
          <h1>대회 목록</h1>
          <div className="spacer" />
          <Link to="/competitions/new" className="create-btn">대회 만들기</Link>
        </div>

        <div className="filters">
          <button className="btn" onClick={() => changeStatus('OPEN')} disabled={status === 'OPEN'}>진행중</button>
          <button className="btn" onClick={() => changeStatus('CLOSED')} disabled={status === 'CLOSED'}>종료</button>
          <button className="btn" onClick={() => changeStatus('DRAFT')} disabled={status === 'DRAFT'}>준비중</button>
        </div>

        <form className="search" onSubmit={onSearch}>
          <input name="keyword" placeholder="검색어" defaultValue={keyword} />
          <button className="btn" type="submit">검색</button>
        </form>

        <div className="grid">
          {loading ? (
            <div style={{ color: '#9ca3af' }}>불러오는 중…</div>
          ) : items.length === 0 ? (
            <div style={{ color: '#9ca3af' }}>표시할 대회가 없습니다.</div>
          ) : (
            items.map((c) => (
              <Link key={c.id} to={`/competitions/${c.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="card">
                  <div className="title">
                    {c.title ?? '제목 없음'}
                    <span className={`badge ${c.status}`}> {c.status}</span>
                  </div>
                  <div className="summary">{c.summary || '설명 없음'}</div>
                  <div className="meta">기간: {c.startAt} ~ {c.endAt}</div>
                </div>
              </Link>
            ))
          )}
        </div>

        <div className="pagination">
          <button className="btn" onClick={() => movePage(Math.max(0, page - 1))} disabled={page <= 0}>이전</button>
          <span className="page-info">페이지 {page + 1} / {totalPages}</span>
          <button className="btn" onClick={() => movePage(Math.min(totalPages - 1, page + 1))} disabled={page + 1 >= totalPages}>다음</button>
        </div>
      </div>
    </Layout>
  );
}
