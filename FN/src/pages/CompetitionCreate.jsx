// src/pages/CompetitionCreate.jsx
import { useState, useEffect } from 'react';
import { useNavigate, Link, useParams } from 'react-router-dom';
import api from '../api/axiosConfig';
import AutoCompetitionButton from '../components/CreateAllCompetitions';

// CompetitionCreate.jsx
import "../css/Competition.scss";
import "../css/CompetitionCreate.scss";

function CompetitionCreate() {
  const navigate = useNavigate();
  const { id } = useParams();          // /competitions/:id/edit 에서 넘어오는 id
  const isEdit = !!id;                 // 있으면 수정 모드, 없으면 생성 모드
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

  // CSV 파일 상태
  const [trainFile, setTrainFile] = useState(null);
  const [testFile, setTestFile] = useState(null);

  const [saving, setSaving] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // ✅ 수정 모드일 때 기존 데이터 불러오기
  useEffect(() => {
    if (!isEdit) return;

    (async () => {
      try {
        const res = await api.get(`/api/competitions/${id}`);
        const c = res.data;

        setForm({
          title: c.title ?? '',
          // 백엔드 DTO에서는 purpose로 내려오므로 여기서 description으로 매핑
          description: c.purpose ?? '',
          detail: c.detail ?? '',
          // LocalDateTime → datetime-local 인풋 포맷으로 변환
          startAt: c.startAt ? String(c.startAt).replace(' ', 'T').slice(0, 16) : '',
          endAt: c.endAt ? String(c.endAt).replace(' ', 'T').slice(0, 16) : '',
          evaluationMetric: c.evaluationMetric ?? 'ACCURACY',
          prizeTotal: c.prizeTotal ?? '',
          status: c.status ?? 'UPCOMING',
        });
      } catch (e) {
        console.error(e);
        alert('대회 정보를 불러오지 못했습니다.');
        navigate('/competitions');
      }
    })();
  }, [isEdit, id, navigate]);

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
    // 🔥 생성 모드일 때만 CSV 필수
    if (!isEdit) {
      if (!trainFile) return 'train.csv 파일을 업로드해주세요.';
      if (!testFile) return 'test.csv 파일을 업로드해주세요.';
    }
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
        status: form.status || 'UPCOMING',
        startAt: normDT(form.startAt),
        endAt: normDT(form.endAt),
        evaluationMetric: form.evaluationMetric || 'ACCURACY',  // ✅
        prizeTotal: form.prizeTotal ? Number(form.prizeTotal) : null // ✅ 숫자로
      };

      if (isEdit) {
        // 🔥 수정 모드: JSON + PUT /api/competitions/{id}
        await api.put(`/api/competitions/${id}`, payload, {
          headers: { 'Content-Type': 'application/json' },
        });
        alert('대회 정보가 수정되었습니다.');
        navigate(`/competitions/${id}`, { replace: true });
      } else {
        // 🔥 생성 모드: multipart/form-data + POST /api/competitions
        const fd = new FormData();
        fd.append(
          "request",
          new Blob([JSON.stringify(payload)], { type: "application/json" })
        );
        fd.append("trainFile", trainFile);
        fd.append("testFile", testFile);
 
        const { data: created } = await api.post('/api/competitions', fd, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
 
        alert(`대회가 등록되었습니다! (ID: ${created.id})`);
        navigate(`/competitions/${created.id}`, { replace: true });
      }
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
      <Link className="back" to="/competitions/List">← 목록으로</Link>
      <h1>{isEdit ? '대회 수정' : '대회 생성'}</h1>

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

        {/* CSV 파일 업로드 */}
        <label>
          Train CSV 업로드
          <input type="file" accept=".csv" onChange={(e) => setTrainFile(e.target.files[0])} />
        </label>
        <label>
          Test CSV 업로드
          <input type="file" accept=".csv" onChange={(e) => setTestFile(e.target.files[0])} />
        </label>

        {errorMsg && <div className="error">{errorMsg}</div>}

        <div className="actions">
          <button type="submit" className="primary" disabled={saving}>
            {saving ? '저장 중...' : (isEdit ? '수정' : '저장')}
          </button>
          <button type="button" onClick={() => navigate('/competitions')} disabled={saving}>
            취소
          </button>
          <AutoCompetitionButton />
        </div>
      </form>
    </div>
  );
}

export default CompetitionCreate;
