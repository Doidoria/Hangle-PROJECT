// src/components/competitions/CompetitionList.jsx
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import axios from 'axios';
// CompetitionList.jsx
import '../../styles/pages/CompetitionList.scss';

function CompetitionList() {
  const [items, setItems] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 12, total: 0 });
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();

  const status = searchParams.get('status') || 'OPEN';
  const keyword = searchParams.get('keyword') || '';
  const page = Number(searchParams.get('page') || 0);
  const size = Number(searchParams.get('size') || 12);

  useEffect(() => {
    async function fetchList() {
      setLoading(true);
      try {
        const res = await axios.get('/api/competitions', {
          params: { status, keyword, page, size },
        });
        // 백엔드 응답 형태 예시:
        // { content: [...], page:0, size:12, totalElements:123 }
        const data = res.data;
        setItems(data.content || []);
        setPageInfo({
          page: data.page ?? page,
          size: data.size ?? size,
          total: data.totalElements ?? 0,
        });
      } catch (e) {
        // 더미 표시
        setItems([
          { id: 1, title: '예시 대회 1', summary: '간단 소개', status: 'OPEN', startAt: '2025-11-01', endAt: '2025-12-01' },
          { id: 2, title: '예시 대회 2', summary: '간단 소개', status: 'OPEN', startAt: '2025-11-10', endAt: '2025-12-15' },
        ]);
        setPageInfo({ page: 0, size: 12, total: 2 });
      } finally {
        setLoading(false);
      }
    }
    fetchList();
  }, [status, keyword, page, size]);

  const onSearch = (e) => {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const nextKeyword = form.get('keyword') || '';
    setSearchParams({ status, keyword: nextKeyword, page: 0, size });
  };

  const changeStatus = (next) => {
    setSearchParams({ status: next, keyword, page: 0, size });
  };

  const movePage = (nextPage) => {
    setSearchParams({ status, keyword, page: nextPage, size });
  };

  const totalPages = Math.max(1, Math.ceil(pageInfo.total / pageInfo.size));

  return (
    // 최상단 div를 이렇게 변경
    <div className="container comp-list">
      <div className="header">
        <h1>대회 목록</h1>
        <div className="spacer" />
        <a href="/admin/competitions/new" className="btn btn-accent">대회 생성</a>
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

      {/* 기존 grid 영역 교체 */}
      <div className="grid">
        {items.length === 0 ? (
          <div style={{ color: '#9ca3af' }}>표시할 대회가 없습니다.</div>
        ) : (
          items.map((c) => (
            <Link key={c.id} to={`/competitions/${c.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div className="card">
                <div className="title">
                  {c.title}
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
        <span className="page-info">페이지 {page + 1} / {Math.max(1, Math.ceil(pageInfo.total / pageInfo.size))}</span>
        <button className="btn" onClick={() => movePage(Math.min(Math.ceil(pageInfo.total / pageInfo.size) - 1, page + 1))}
          disabled={page + 1 >= Math.max(1, Math.ceil(pageInfo.total / pageInfo.size))}>
          다음
        </button>
      </div>
    </div>
  );
}

export default CompetitionList;
