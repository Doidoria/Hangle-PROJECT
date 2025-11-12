import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import Swal from 'sweetalert2';
import { useNavigate } from 'react-router-dom';
import '../css/MyInquiries.scss';

function MyInquiries() {
    const [inquiries, setInquiries] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('전체');
    const [sortOrder, setSortOrder] = useState('desc');
    const [dateFilter, setDateFilter] = useState('전체');
    const [modalData, setModalData] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchMyInquiries();
    }, []);

    const fetchMyInquiries = async () => {
        setLoading(true);
        try {
            const res = await api.get('/api/inquiry/my');
            setInquiries(res.data);
            setFiltered(res.data);
        } catch (err) {
            Swal.fire({ icon: 'error', title: '조회 실패', text: '문의 목록을 불러올 수 없습니다.' });
        } finally {
            setLoading(false);
        }
    };

    // 필터 적용
    useEffect(() => {
        let data = [...inquiries];

        // 검색
        if (search.trim()) {
            data = data.filter(i => i.title.toLowerCase().includes(search.toLowerCase()));
        }

        // 상태 필터
        if (statusFilter !== '전체') {
            data = data.filter(i =>
                statusFilter === '답변완료' ? i.answeredAt : !i.answeredAt
            );
        }

        // 기간 필터
        if (dateFilter !== '전체') {
            const now = new Date();
            const compareDate = new Date(
                dateFilter === '1개월' ? now.setMonth(now.getMonth() - 1)
                    : now.setMonth(now.getMonth() - 3)
            );
            data = data.filter(i => new Date(i.createdAt) >= compareDate);
        }

        // 정렬
        data.sort((a, b) =>
            sortOrder === 'desc'
                ? new Date(b.createdAt) - new Date(a.createdAt)
                : new Date(a.createdAt) - new Date(b.createdAt)
        );

        setFiltered(data);
    }, [search, statusFilter, sortOrder, dateFilter, inquiries]);

    // 통계 계산
    const totalCount = inquiries.length;
    const answeredCount = inquiries.filter(i => i.answeredAt).length;
    const pendingCount = totalCount - answeredCount;

    const getStatusText = answeredAt => (answeredAt ? '답변 완료' : '접수');

    return (
        <div className="my-inquiries-container">
            <h2>나의 1:1 문의 내역</h2>

            {/* 통계 카드 */}
            <div className="inquiry-stats">
                <div className="stat-card total">전체 <span>{totalCount}</span></div>
                <div className="stat-card pending">대기 <span>{pendingCount}</span></div>
                <div className="stat-card done">완료 <span>{answeredCount}</span></div>
            </div>

            {/* 필터 영역 */}
            <div className="inquiry-filters">
                <input
                    type="text"
                    placeholder="문의 제목 검색"
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
                    <option>전체</option>
                    <option>답변대기</option>
                    <option>답변완료</option>
                </select>
                <select value={dateFilter} onChange={e => setDateFilter(e.target.value)}>
                    <option>전체</option>
                    <option>1개월</option>
                    <option>3개월</option>
                </select>
                <select value={sortOrder} onChange={e => setSortOrder(e.target.value)}>
                    <option value="desc">최신순</option>
                    <option value="asc">오래된순</option>
                </select>
            </div>

            {/* 목록 */}
            {loading ? (
                <div className="loading">목록을 불러오는 중...</div>
            ) : filtered.length === 0 ? (
                <p className="no-data">문의 내역이 없습니다.</p>
            ) : (
                <table className="inquiry-table">
                    <thead>
                        <tr>
                            <th>번호</th>
                            <th>제목</th>
                            <th>작성일</th>
                            <th>상태</th>
                            <th>답변일</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map((inq) => (
                            <tr key={inq.id} onClick={() => setModalData(inq)}>
                                <td>{inq.id}</td>
                                <td className="title">{inq.title}</td>
                                <td>{new Date(inq.createdAt).toLocaleDateString()}</td>
                                <td className={inq.answeredAt ? 'status done' : 'status pending'}>
                                    {getStatusText(inq.answeredAt)}
                                </td>
                                <td>{inq.answeredAt ? new Date(inq.answeredAt).toLocaleDateString() : '-'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}

            {/* 문의작성 버튼 */}
            <button className="floating-btn" onClick={() => navigate('/inquiry/write')}>
                <span class="material-symbols-outlined">edit_square</span>
                <span>문의 작성</span>
            </button>

            {/* 모달 */}
            {modalData && (
                <div className="modal-overlay" onClick={() => setModalData(null)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <h3>{modalData.title}</h3>
                        <p className="modal-date">
                            작성일: {new Date(modalData.createdAt).toLocaleDateString()}
                        </p>
                        <div className="modal-section">
                            <strong>문의 내용</strong>
                            <p>{modalData.content}</p>
                        </div>
                        <div className="modal-section">
                            <strong>답변</strong>
                            <p>{modalData.answer || '아직 답변이 등록되지 않았습니다.'}</p>
                        </div>
                        <button onClick={() => setModalData(null)}>닫기</button>
                    </div>
                </div>
            )}
        </div>
    );
}

export default MyInquiries;
