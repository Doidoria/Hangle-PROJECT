// src/pages/CompetitionCreate.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosConfig';
import '../css/competitionStyle/pages/CompetitionCreate.scss';

function CompetitionCreate() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '',
    description: '',        // 목적(한 줄) -> backend purpose
    detail: '',             // ✅ 상세 설명
    startAt: '',            // "YYYY-MM-DDTHH:mm"
    endAt: '',
    evaluationMetric: 'ACCURACY', // ✅ 기본값
    prizeTotal: '',         // ✅ 숫자 입력
    // 화면엔 안 보여도 전송은 해야 함(백엔드 @NotNull): 
    status: 'UPCOMING',
  });
  const [saving, setSaving] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
  };

  const normDT = (v) => (v ? (v.length === 16 ? `${v}:00` : v) : null);

  const validate = () => {
    if (!form.title.trim()) return '제목을 입력해주세요.';
    if (!form.description.trim()) return '목적을 입력해주세요.';
    if (!form.startAt || !form.endAt) return '시작일과 종료일을 입력해주세요.';
    if (form.endAt < form.startAt) return '종료일은 시작일 이후여야 합니다.';
    if (form.prizeTotal && Number.isNaN(Number(form.prizeTotal))) return '상금은 숫자만 입력해주세요.';
    return null;
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    if (saving) return;
    setErrorMsg('');
    const v = validate();
    if (v) { setErrorMsg(v); return; }

    try {
      setSaving(true);
      const payload = {
        title: form.title.trim(),
        description: form.description?.trim() || null,
        detail: form.detail?.trim() || null,               // ✅ 상세 설명
        status: 'UPCOMING',                                 // 숨김 기본값
        startAt: normDT(form.startAt),
        endAt: normDT(form.endAt),
        evaluationMetric: form.evaluationMetric || 'ACCURACY',  // ✅
        prizeTotal: form.prizeTotal ? Number(form.prizeTotal) : null // ✅ 숫자로
      };

      // baseURL이 /api를 포함한다면 아래 경로는 '/competitions'로 바꿔주세요.
      const { data: created } = await api.post('/api/competitions', payload, {
        headers: { 'Content-Type': 'application/json' },
      });

      alert(`대회가 등록되었습니다! (ID: ${created.id})`);
      navigate(`/competitions/${created.id}`, { replace: true });
    } catch (err) {
      console.error(err);
      if (err.code === 'ERR_NETWORK') return setErrorMsg('서버에 연결할 수 없습니다. (네트워크 오류)');
      const msg = err.response?.data?.message || err.response?.data?.error ||
        `저장 중 오류가 발생했습니다. (HTTP ${err.response?.status ?? '???'})`;
      setErrorMsg(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="container comp-create">
      <Link className="back" to="/competitions">← 목록으로</Link>
      <h1>대회 생성</h1>

      <form onSubmit={onSubmit} noValidate>
        <label>
          제목
          <input name="title" value={form.title} onChange={onChange} required placeholder="대회 제목" />
        </label>

        <label>
          목적(한 줄)
          <input name="description" value={form.description} onChange={onChange} required placeholder="예) 고양이/강아지 분류 모델 개발" />
        </label>

        <label>
          상세 설명
          <textarea name="detail" value={form.detail} onChange={onChange} rows={8} placeholder="대회의 상세 목표/데이터 설명/제출 형식 등" />
        </label>

        <div className="row">
          <label>
            시작일
            <input type="datetime-local" name="startAt" value={form.startAt} onChange={onChange} required />
          </label>
          <label>
            종료일
            <input type="datetime-local" name="endAt" value={form.endAt} onChange={onChange} min={form.startAt || undefined} required />
          </label>
        </div>

        <label>
          평가 지표
          <select name="evaluationMetric" value={form.evaluationMetric} onChange={onChange}>
            <option value="ACCURACY">ACCURACY</option>
            <option value="F1">F1</option>
            <option value="AUC">AUC</option>
            <option value="RMSE">RMSE</option>
            <option value="MAE">MAE</option>
          </select>
        </label>

        <label>
          상금
          <input type="number" step="0.01" name="prizeTotal" value={form.prizeTotal} onChange={onChange} placeholder="예: 1000000" />
        </label>

        {errorMsg && <div className="error">{errorMsg}</div>}

        <div className="actions">
          <button type="submit" className="primary" disabled={saving}>
            {saving ? '저장 중...' : '저장'}
          </button>
          <button type="button" onClick={() => navigate('/competitions')} disabled={saving}>
            취소
          </button>
        </div>
      </form>
    </div>
  );
}

export default CompetitionCreate;
