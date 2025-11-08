// src/components/competitions/CompetitionCreate.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

import '../css/competitionStyle/pages/CompetitionCreate.scss';

import Layout from './Layout.jsx';

function CompetitionCreate() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '',
    summary: '',
    description: '',
    prize: '',
    startAt: '',
    endAt: '',
    status: 'DRAFT',
    datasetUrl: '',
    rulesUrl: '',
  });
  const [saving, setSaving] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    if (saving) return; // 중복 제출 방지
    setErrorMsg('');

    if (!form.title.trim()) {
      setErrorMsg('제목을 입력해주세요.');
      return;
    }
    if (!form.startAt || !form.endAt) {
      setErrorMsg('시작일과 종료일을 입력해주세요.');
      return;
    }
    if (form.endAt < form.startAt) {
      setErrorMsg('종료일은 시작일 이후여야 합니다.');
      return;
    }

    try {
      setSaving(true);
      const res = await axios.post('http://localhost:8095/api/competitions', form, {
        headers: { 'Content-Type': 'application/json' },
      });
      const created = res.data;
      alert(`대회가 등록되었습니다!\nID: ${created.id}`);
      navigate(`/competitions/${created.id}`);
    } catch (err) {
      console.error(err);
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        '저장 중 오류가 발생했습니다.';
      setErrorMsg(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Layout>
      <div className="container comp-create">
        <Link className="back" to="/Competitions">← 목록으로</Link>
        <h1>대회 생성</h1>

        <form onSubmit={onSubmit}>
          <label>
            제목
            <input name="title" value={form.title} onChange={onChange} placeholder="대회 제목" />
          </label>

          <label>
            요약
            <input name="summary" value={form.summary} onChange={onChange} placeholder="간단 소개" />
          </label>

          <label>
            설명
            <textarea name="description" value={form.description} onChange={onChange} rows={8} placeholder="상세 설명" />
          </label>

          <div className="row">
            <label>
              시작일
              <input type="date" name="startAt" value={form.startAt} onChange={onChange} />
            </label>
            <label>
              종료일
              <input type="date" name="endAt" value={form.endAt} onChange={onChange} />
            </label>
          </div>

          <label>
            상금
            <input name="prize" value={form.prize} onChange={onChange} placeholder="예: 총상금 100만원" />
          </label>

          <label>
            상태
            <select name="status" value={form.status} onChange={onChange}>
              <option value="DRAFT">DRAFT</option>
              <option value="OPEN">OPEN</option>
              <option value="CLOSED">CLOSED</option>
            </select>
          </label>

          <label>
            데이터셋 URL
            <input name="datasetUrl" value={form.datasetUrl} onChange={onChange} placeholder="https://..." />
          </label>

          <label>
            규칙 URL
            <input name="rulesUrl" value={form.rulesUrl} onChange={onChange} placeholder="https://..." />
          </label>

          {errorMsg && <div className="error">{errorMsg}</div>}

          <div className="actions">
            <button type="submit" className="primary" disabled={saving}>
              {saving ? '저장 중...' : '저장'}
            </button>
            <button type="button" onClick={() => navigate('/competitions')}>
              취소
            </button>
          </div>
        </form>
      </div>
    </Layout>
  );
}
export default CompetitionCreate;
