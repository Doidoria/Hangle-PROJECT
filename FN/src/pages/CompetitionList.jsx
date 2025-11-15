import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

function fmtDT(v) {
  if (!v) return '-';
  // "2025-12-01T18:00:00" → "2025-12-01 18:00"
  return v.replace('T', ' ').slice(0, 16);
}

export default function CompetitionList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  // URL 쿼리 ↔ 상태
  const status = searchParams.get('status') || '';
  const keyword = searchParams.get('keyword') || '';
  const page = Number(searchParams.get('page') || 0);
  const size = Number(searchParams.get('size') || 12);

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [data, setData] = useState({ content: [], totalPages: 0, number: 0, totalElements: 0 });

  const params = useMemo(() => {
    const p = { page, size };
    if (status) p.status = status; // UPCOMING | OPEN | CLOSED
    if (keyword.trim()) p.keyword = keyword.trim();
    return p;
  }, [status, keyword, page, size]);

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        setErrorMsg('');
        // NOTE: axiosConfig.baseURL이 http://localhost:8090 라면 경로에 /api 포함해야 함
        const res = await api.get('/api/competitions', { params });
        setData(res.data);
      } catch (err) {
        console.error(err);
        setErrorMsg('목록을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    })();
  }, [params]);

  const onSearch = (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const next = new URLSearchParams(searchParams);
    next.set('page', '0'); // 새 검색은 첫 페이지부터
    next.set('size', String(size));
    next.set('status', fd.get('status') || '');
    next.set('keyword', fd.get('keyword') || '');
    setSearchParams(next);
  };

  const movePage = (nextPage) => {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(nextPage));
    setSearchParams(next);
  };

  return (
    <section className="container" style={{padding:'16px'}}>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:12}}>
        <h1 style={{margin:0}}>대회 목록</h1>
        <Link className="button" to="/competitions/new">대회 생성</Link>
      </div>

      {/* 검색/필터 */}
      <form onSubmit={onSearch} style={{display:'grid', gap:8, gridTemplateColumns:'150px 1fr 120px', marginBottom:12}}>
        <select name="status" defaultValue={status}>
          <option value="">상태(전체)</option>
          <option value="UPCOMING">UPCOMING</option>
          <option value="OPEN">OPEN</option>
          <option value="CLOSED">CLOSED</option>
        </select>
        <input name="keyword" defaultValue={keyword} placeholder="제목 검색" />
        <button type="submit">검색</button>
      </form>

      {/* 에러/로딩 */}
      {errorMsg && <div style={{color:'#c00', marginBottom:8}}>{errorMsg}</div>}
      {loading && <div style={{marginBottom:8}}>불러오는 중…</div>}

      {/* 목록 */}
      <div style={{overflowX:'auto', border:'1px solid var(--border, #e5e7eb)', borderRadius:8}}>
        <table style={{width:'100%', borderCollapse:'collapse'}}>
          <thead style={{background:'var(--surface, #f9fafb)'}}>
            <tr>
              <th style={th}>ID</th>
              <th style={th}>제목</th>
              <th style={th}>목적</th>
              <th style={th}>상태</th>
              <th style={th}>시작</th>
              <th style={th}>종료</th>
              <th style={th}>지표</th>
              <th style={th}>상금</th>
              <th style={th}>참가자</th>
              <th style={th}></th>
            </tr>
          </thead>
          <tbody>
            {data.content.length === 0 && !loading && (
              <tr><td colSpan={10} style={{textAlign:'center', padding:'16px'}}>데이터가 없습니다.</td></tr>
            )}
            {data.content.map((it) => (
              <tr key={it.id} style={{borderTop:'1px solid var(--border, #e5e7eb)'}}>
                <td style={td}>{it.id}</td>
                <td style={td}>{it.title}</td>
                <td style={td}>{it.purpose || '-'}</td>
                <td style={td}>{it.status}</td>
                <td style={td}>{fmtDT(it.startAt)}</td>
                <td style={td}>{fmtDT(it.endAt)}</td>
                <td style={td}>{it.evaluationMetric || '-'}</td>
                <td style={td}>{it.prizeTotal != null ? Number(it.prizeTotal).toLocaleString() : '-'}</td>
                <td style={td}>{it.participantCount ?? 0}</td>
                <td style={td}>
                  <Link to={`/competitions/${it.id}`}>상세</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 페이지네이션 */}
      <div style={{display:'flex', gap:8, justifyContent:'center', marginTop:12}}>
        <button disabled={page <= 0} onClick={() => movePage(page - 1)}>이전</button>
        <span>Page {page + 1} / {Math.max(data.totalPages, 1)}</span>
        <button disabled={page + 1 >= data.totalPages} onClick={() => movePage(page + 1)}>다음</button>
      </div>
    </section>
  );
}

const th = { textAlign:'left', padding:'10px 12px', fontWeight:700, fontSize:14 };
const td = { padding:'10px 12px', fontSize:14 };
