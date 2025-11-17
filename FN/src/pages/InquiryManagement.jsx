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
                const updatedItem = res.data.find(i => i.id === modalData.id);
                setModalData(updatedItem);
            }
        } catch (err) {
            Swal.fire({ icon: 'error', title: '조회 실패', text: '전체 문의 목록을 불러올 수 없습니다.' });
        } finally {
            setLoading(false);
        }
    };

    // 필터 적용 (userName 포함 검색 로직으로 수정)
    useEffect(() => {
        let data = [...inquiries];

        // 검색
        if (search.trim()) {
            const lowerSearch = search.toLowerCase();
            data = data.filter(i =>
                i.title.toLowerCase().includes(lowerSearch) ||
                // userLoginId 대신 userName 필드를 사용하여 검색
                (i.userName && i.userName.toLowerCase().includes(lowerSearch))
            );
        }

        // 상태 필터 
        if (statusFilter !== '전체') {
            const filterStatus = statusFilter === '답변완료' ? 'ANSWERED' : 'PENDING';
            data = data.filter(i => i.status === filterStatus);
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
    }, [search, statusFilter, sortOrder, dateFilter, inquiries, modalData]);

    // 통계 계산
    const totalCount = inquiries.length;
    const answeredCount = inquiries.filter(i => i.status === 'ANSWERED').length;
    const pendingCount = totalCount - answeredCount;

    // 상태 텍스트
    const getStatusText = (status) => {
        if (status === 'ANSWERED') return '답변 완료';
        if (status === 'PENDING') return '답변 대기';
        return '알 수 없음';
    };

    // 관리자 삭제 로직
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
            } catch (err) {
                Swal.fire('삭제 실패', '서버 오류로 삭제할 수 없습니다.', 'error');
            }
        }
    };

    // 답변 등록 로직
    const handleAnswerSubmit = async (inquiryId) => {
        if (!answerInput.trim()) {
            Swal.fire('경고', '답변 내용을 입력해주세요.', 'warning');
            return;
        }

        try {
            await api.post(`/api/inquiry/${inquiryId}/answer`, {
                answerContent: answerInput
            });

            Swal.fire('답변 등록 완료', '답변이 성공적으로 등록되었습니다.', 'success');

            setAnswerInput('');
            fetchAllInquiries();
        } catch (err) {
            Swal.fire('등록 실패', '답변 등록 중 서버 오류가 발생했습니다.', 'error');
        }
    };

    // 모달 오픈 시 답변 내용을 미리 로드
    const openModal = (inq) => {
        setModalData(inq);
        setAnswerInput(inq.answerContent || '');
    }


    return (
        <div className="my-inquiries-container">
            <button
                className="go-faq-btn"
                onClick={() => navigate("/")}
            >
                <span className="material-symbols-outlined">arrow_left</span>
                <span className="material-symbols-outlined">home</span>
            </button>
            <h2>1:1 문의 관리 페이지</h2>

            {/* 통계 카드 */}
            <div className="inquiry-stats">
                <div className="stat-card total">전체 문의 <span>{totalCount}</span></div>
                <div className="stat-card pending">답변 대기 <span>{pendingCount}</span></div>
                <div className="stat-card done">답변 완료 <span>{answeredCount}</span></div>
            </div>

            {/* 필터 영역 */}
            <div className="inquiry-filters">
                <input
                    type="text"
                    placeholder="제목/작성자 ID 검색"
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
                            <th>작성자 ID</th>
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
                                <td className="title">
                                    {inq.title}
                                </td>
                                {/* inq.userName 사용 */}
                                <td>{inq.userName || '(탈퇴한 사용자)'}</td>
                                <td>{new Date(inq.createdAt).toLocaleDateString()}</td>
                                <td className={inq.status === 'ANSWERED' ? 'status done' : 'status pending'}>
                                    {getStatusText(inq.status)}
                                </td>
                                <td>{inq.answerDate ? new Date(inq.answerDate).toLocaleDateString() : '-'}</td>
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
                            문의 ID: {modalData.id} |
                            {/* modalData.userName 사용 */}
                            작성자 ID: {modalData.userName || '(탈퇴한 사용자)'} |
                            작성일: {new Date(modalData.createdAt).toLocaleDateString()}
                        </p>
                        <h3>{modalData.title}</h3>

                        {/* 문의 내용 카드 */}
                        <div className="modal-card">
                            <strong>문의 내용</strong>
                            <p>
                                <span className="material-symbols-outlined faq-icon">question_mark</span>
                                {modalData.content}
                            </p>
                        </div>

                        {/* 답변 내용 카드 / 답변 입력 필드 */}
                        <div className="modal-card admin-answer-section">
                            <strong>답변 입력/수정 ({getStatusText(modalData.status)})</strong>
                            <div className="answer-input-container">
                                <span className="material-symbols-outlined faq-icon">campaign</span>
                                <textarea
                                    className="admin-answer-textarea"
                                    value={answerInput}
                                    onChange={(e) => setAnswerInput(e.target.value)}
                                    placeholder="관리자 답변을 입력하세요."
                                    rows="5"
                                />
                            </div>

                            {/* 답변 완료 정보 표시 */}
                            {modalData.answerDate && (
                                <p className="answer-info">
                                    최종 답변일: {new Date(modalData.answerDate).toLocaleString()}
                                </p>
                            )}
                        </div>

                        {/* 답변 완료 버튼 & 닫기 */}
                        <div className="modal-actions">
                            <button
                                className="answer-submit-btn" // 주 컬러 버튼
                                onClick={() => handleAnswerSubmit(modalData.id)}
                            >
                                <span className="material-symbols-outlined">send</span>
                                {modalData.answerContent ? '답변 수정' : '답변 등록'}
                            </button>
                            <button onClick={() => setModalData(null)}>
                                <span className="material-symbols-outlined">close</span>
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