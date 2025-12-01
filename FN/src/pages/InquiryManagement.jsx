import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import Swal from 'sweetalert2';
import { useNavigate } from 'react-router-dom';
import '../css/InquiryManagement.scss';

function InquiryManagement() {
    const [inquiries, setInquiries] = useState([]);
    const [filtered, setFiltered] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('전체');
    const [sortOrder, setSortOrder] = useState('desc');
    const [dateFilter, setDateFilter] = useState('전체');
    const [modalData, setModalData] = useState(null);
    const [answerInput, setAnswerInput] = useState('');
    const navigate = useNavigate();

    // 이름 마스킹
    const maskName = (name) => {
        if (!name) return "(탈퇴한 사용자)";
        return name.charAt(0) + "**";
    };

    useEffect(() => {
        fetchAllInquiries();
    }, []);

    // 전체 문의 조회
    const fetchAllInquiries = async () => {
        setLoading(true);
        try {
            const res = await api.get('/api/inquiry/admin');
            setInquiries(res.data);
            setFiltered(res.data);

            if (modalData) {
                const updated = res.data.find(i => i.id === modalData.id);
                setModalData(updated);
            }
        } catch (err) {
            Swal.fire({ icon: 'error', title: '조회 실패', text: '전체 문의 목록을 불러올 수 없습니다.' });
        } finally {
            setLoading(false);
        }
    };

    // 필터 + 정렬 적용
    useEffect(() => {
        let data = [...inquiries];

        // 검색
        if (search.trim()) {
            const q = search.toLowerCase();
            data = data.filter(i =>
                i.title.toLowerCase().includes(q) ||
                (i.username && i.username.toLowerCase().includes(q))
            );
        }

        // 상태 필터
        if (statusFilter !== '전체') {
            const s = statusFilter === '답변완료' ? 'ANSWERED' : 'PENDING';
            data = data.filter(i => i.status === s);
        }

        // 기간 필터
        if (dateFilter !== '전체') {
            const now = new Date();
            const compare = new Date(
                dateFilter === '1개월'
                    ? now.setMonth(now.getMonth() - 1)
                    : now.setMonth(now.getMonth() - 3)
            );
            data = data.filter(i => new Date(i.createdAt) >= compare);
        }

        // 상태 우선 정렬 (답변대기 → 답변완료) + 날짜 정렬
        data.sort((a, b) => {
            // 상태가 다르면 PENDING 먼저
            if (a.status !== b.status) {
                return a.status === 'PENDING' ? -1 : 1;
            }

            // 같은 상태이면 날짜 정렬
            return sortOrder === 'desc'
                ? new Date(b.createdAt) - new Date(a.createdAt)
                : new Date(a.createdAt) - new Date(b.createdAt);
        });

        setFiltered(data);
    }, [search, statusFilter, sortOrder, dateFilter, inquiries]);

    // 통계
    const totalCount = inquiries.length;
    const answeredCount = inquiries.filter(i => i.status === 'ANSWERED').length;
    const pendingCount = totalCount - answeredCount;

    const getStatusText = (status) =>
        status === 'ANSWERED' ? '답변 완료' : '답변 대기';

    // 삭제
    const handleDeleteByAdmin = async (id, e) => {
        e.stopPropagation();

        const result = await Swal.fire({
            title: '문의 삭제 (관리자)',
            text: '정말로 이 문의를 삭제하시겠습니까?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: '삭제',
            cancelButtonText: '취소',
            confirmButtonColor: '#e11d48',
        });

        if (result.isConfirmed) {
            try {
                await api.delete(`/api/inquiry/admin/${id}`);
                Swal.fire('삭제 완료', '문의가 삭제되었습니다.', 'success');
                fetchAllInquiries();
            } catch {
                Swal.fire('삭제 실패', '서버 오류가 발생했습니다.', 'error');
            }
        }
    };

    // 답변 등록/수정
    const handleAnswerSubmit = async (id) => {
        if (!answerInput.trim()) {
            Swal.fire('경고', '답변 내용을 입력해주세요.', 'warning');
            return;
        }

        try {
            await api.post(`/api/inquiry/admin/${id}/answer`, {
                answerContent: answerInput
            });

            Swal.fire('완료', '답변이 등록/수정되었습니다.', 'success');
            setAnswerInput('');
            fetchAllInquiries();
        } catch {
            Swal.fire('실패', '답변 등록 중 오류가 발생했습니다.', 'error');
        }
    };

    const openModal = (inq) => {
        setModalData(inq);
        setAnswerInput(inq.answerContent || '');
    };

    const formatDate = (dateValue) => {
        if (!dateValue) return '-';
        if (Array.isArray(dateValue)) {
            return new Date(dateValue[0], dateValue[1] - 1, dateValue[2]).toLocaleDateString();
        }
        return new Date(dateValue).toLocaleDateString();
    };

    return (
        <div className="my-inquiries-container">
            <button className="go-faq-btn" onClick={() => navigate("/")}>
                <span className="material-symbols-outlined">arrow_left</span>
                <span className="material-symbols-outlined">home</span>
            </button>

            <h2>1:1 문의 관리 페이지</h2>

            {/* 카드 필터 버튼 */}
            <div className="inquiry-stats">
                <button
                    className={`stat-card total ${statusFilter === '전체' ? 'active' : ''}`}
                    onClick={() => setStatusFilter('전체')}
                >
                    전체 <span>{totalCount}</span>
                </button>

                <button
                    className={`stat-card pending ${statusFilter === '답변대기' ? 'active' : ''}`}
                    onClick={() => setStatusFilter('답변대기')}
                >
                    대기 <span>{pendingCount}</span>
                </button>

                <button
                    className={`stat-card done ${statusFilter === '답변완료' ? 'active' : ''}`}
                    onClick={() => setStatusFilter('답변완료')}
                >
                    완료 <span>{answeredCount}</span>
                </button>
            </div>

            {/* 필터 영역 */}
            <div className="inquiry-filters">
                <input
                    type="text"
                    placeholder="제목/작성자 검색"
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
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
                            <th>작성자</th>
                            <th>작성일</th>
                            <th>상태</th>
                            <th>답변일</th>
                            <th> - </th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map((inq, index) => (
                            <tr key={inq.id} onClick={() => openModal(inq)}>
                                <td>{index + 1}</td>
                                <td className="title">{inq.title}</td>
                                <td>{maskName(inq.username)}</td>
                                <td>{formatDate(inq.answerDate)}</td>
                                <td className={inq.status === 'ANSWERED' ? 'status done' : 'status pending'}>
                                    {getStatusText(inq.status)}
                                </td>
                                <td>{inq.answerDate ? formatDate(inq.answerDate) : '-'}</td>
                                <td>
                                    <button
                                        className="delete-btn"
                                        onClick={(e) => handleDeleteByAdmin(inq.id, e)}
                                    >
                                        삭제
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}

            {/* 모달 */}
            {modalData && (
                <div className="modal-overlay" onClick={() => setModalData(null)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>

                        <p className="modal-date">
                            작성일: {new Date(modalData.answerDate).toLocaleDateString()}
                        </p>

                        <h3>{modalData.title}</h3>

                        <div className="modal-card">
                            <strong>문의 내용</strong>
                            <p>
                                <span className="material-symbols-outlined faq-icon">question_mark</span>
                                {modalData.content}
                            </p>
                        </div>

                        <div className="modal-card">
                            <strong>답변 입력/수정</strong>
                            <div className="admin-answer-box">
                                <span className="material-symbols-outlined faq-icon">campaign</span>
                                <textarea
                                    className="admin-answer-textarea"
                                    value={answerInput}
                                    placeholder="관리자 답변을 입력하세요."
                                    onChange={(e) => setAnswerInput(e.target.value)}
                                    rows="5"
                                ></textarea>
                            </div>

                            {modalData.answerDate && (
                                <p className="answer-date">
                                    답변일: {new Date(modalData.answerDate).toLocaleDateString()}
                                </p>
                            )}
                        </div>

                        <div className="modal-buttons">
                            <button
                                className="edit-btn"
                                onClick={() => handleAnswerSubmit(modalData.id)}
                            >
                                <span className="material-symbols-outlined faq-modal-icon">edit</span>
                                {modalData.answerContent ? "답변 수정" : "답변 등록"}
                            </button>

                            <button className="close-btn" onClick={() => setModalData(null)}>
                                <span className="material-symbols-outlined faq-modal-icon">close</span>
                                닫기
                            </button>
                        </div>

                    </div>
                </div>
            )}
        </div>
    );
}

export default InquiryManagement;
