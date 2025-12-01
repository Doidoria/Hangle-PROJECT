import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

// CompetitionList.jsx
import "../css/Competition.scss";
import "../css/CompetitionList.scss";

function fmtDT(v) {
  if (!v) return '-';

  if (Array.isArray(v)) {
    const [year, month, day, hour = 0, minute = 0] = v; // 시, 분이 없을 경우 0 처리
    const p = (n) => String(n).padStart(2, '0'); // 0 채우기 (1 -> 01)
    return `${year}.${p(month)}.${p(day)} - ${p(hour)}:${p(minute)}`;
  }

  const str = String(v);
  if (str.includes('T')) {
    const d = new Date(str);
    const p = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}.${p(d.getMonth() + 1)}.${p(d.getDate())} - ${p(d.getHours())}:${p(d.getMinutes())}`;
  }

  // 혹시 다른 포맷이면 Date 파싱 시도
  const d = new Date(str);
  if (!Number.isNaN(d.getTime())) {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  // 그래도 안 되면 원본 그대로
  return str;
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

  const onEdit = (id) => {
    // 편하게 /competitions/:id/edit 라우트로 보낼게
    navigate(`/competitions/${id}/edit`);
  };

  const onDelete = async (id) => {
    if (!window.confirm(`대회 ID ${id} 를 정말 삭제할까요?`)) return;

    try {
      await api.delete(`/api/competitions/${id}`);
      alert('삭제되었습니다.');

      // 가장 간단한 방법: 현재 페이지 다시 로드
      // (조금 더 깔끔하게 하려면 fetch 함수를 분리해서 다시 호출)
      window.location.reload();

      // 또는 데이터만 갱신하고 싶으면:
      // setData(prev => ({
      //   ...prev,
      //   content: prev.content.filter(c => c.id !== id),
      //   totalElements: prev.totalElements - 1,
      // }));
    } catch (err) {
      console.error(err);
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        `삭제 중 오류가 발생했습니다. (HTTP ${err.response?.status ?? '???'})`;
      alert(msg);
    }
  };



  return (
    <section className="container comp-list">
      <div className="comp-list__header">
        <h1 className="comp-list__title">대회 목록</h1>
        <Link
          className="btn btn-primary comp-list__create-btn"
          to="/competitions/new"
        >
          대회 생성
        </Link>
      </div>

      {/* 검색/필터 */}
      <form onSubmit={onSearch} className="comp-list__toolbar">
        <select name="status" defaultValue={status}>
          <option value="">상태(전체)</option>
          <option value="UPCOMING">UPCOMING</option>
          <option value="OPEN">OPEN</option>
          <option value="CLOSED">CLOSED</option>
        </select>

        <input
          name="keyword"
          defaultValue={keyword}
          placeholder="제목 검색"
        />

        <button type="submit" className="btn btn-ghost">
          검색
        </button>
      </form>

      {/* 에러/로딩 */}
      {errorMsg && (
        <div className="comp-list__state comp-list__state--error">
          {errorMsg}
        </div>
      )}
      {loading && (
        <div className="comp-list__state">
          불러오는 중…
        </div>
      )}

      {/* 목록 */}
      <div className="comp-list__table-wrapper">
        <table className="comp-list__table">
          <thead>
            <tr>
              <th>ID</th>
              <th>제목</th>
              <th>목적</th>
              <th>상태</th>
              <th>시작</th>
              <th>종료</th>
              <th>지표</th>
              <th>상금</th>
              <th>참가자</th>
              <th></th>
            </tr>
          </thead>

          <tbody>
            {data.content.length === 0 && !loading && (
              <tr>
                <td colSpan={10} className="comp-list__empty">
                  데이터가 없습니다.
                </td>
              </tr>
            )}
            {data.content.map((it) => (
              <tr key={it.id}>
                <td>{it.id}</td>
                <td>{it.title}</td>
                <td>{it.purpose || '-'}</td>
                <td>{it.status}</td>
                <td>{fmtDT(it.startAt)}</td>
                <td>{fmtDT(it.endAt)}</td>
                <td>{it.evaluationMetric || '-'}</td>
                <td>{it.prizeTotal != null ? Number(it.prizeTotal).toLocaleString() : '-'}</td>
                <td>{it.participantCount ?? 0}</td>
                <td>
                  <div className="comp-list__actions">
                    <Link
                      to={`/competitions/${it.id}`}
                      className="btn btn-ghost"
                    >
                      상세
                    </Link>
                    <button
                      type="button"
                      className="btn btn-ghost"
                      onClick={() => onEdit(it.id)}
                    >
                      수정
                    </button>
                    <button
                      type="button"
                      className="btn btn-ghost comp-list__btn-danger"
                      onClick={() => onDelete(it.id)}
                    >
                      삭제
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>

        </table>
      </div>

      {/* 페이지네이션 */}
      <div className="comp-list__pagination">
        <button
          type="button"
          className="btn btn-ghost"
          disabled={page <= 0}
          onClick={() => movePage(page - 1)}
        >
          이전
        </button>
        <span>Page {page + 1} / {Math.max(data.totalPages, 1)}</span>
        <button
          type="button"
          className="btn btn-ghost"
          disabled={page + 1 >= data.totalPages}
          onClick={() => movePage(page + 1)}
        >
          다음
        </button>
      </div>

    </section>
  );
}
