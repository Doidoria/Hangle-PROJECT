// src/pages/MyInquiries.jsx

import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig'; // 실제 파일명에 맞춰 axiosConfig로 import
import { useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';

function MyInquiries() {
    const [inquiries, setInquiries] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        fetchMyInquiries();
    }, []);

    const fetchMyInquiries = async () => {
        setLoading(true);
        try {
            // 백엔드 API 호출 (GET /api/inquiry/my)
            const response = await api.get('/api/inquiry/my');

            // InquiryResponseDto (id, title, createdAt, answeredAt, status)를 가정
            setInquiries(response.data);
            console.log('문의 목록 로드 성공:', response.data);

        } catch (error) {
            console.error('문의 목록 로드 실패:', error);
            // 인증 오류 등은 인터셉터가 처리하므로, 여기서는 로딩 실패 메시지만 출력
            Swal.fire({
                icon: 'error',
                title: '조회 실패',
                text: '문의 목록을 불러오는 데 실패했습니다.',
            });
        } finally {
            setLoading(false);
        }
    };

    // 답변 상태를 보기 좋게 변환
    const getStatusText = (answeredAt) => {
        return answeredAt ? '답변 완료' : '접수';
    };

    if (loading) {
        return <div style={{ textAlign: 'center', padding: '20px' }}>목록을 불러오는 중...</div>;
    }

    return (
        <div className="my-inquiries-container">
            <h2>나의 1:1 문의 내역</h2>

            <div style={{ textAlign: 'right', marginBottom: '15px' }}>
                <button
                    onClick={() => navigate('/inquiry/write')}
                    style={{ padding: '10px 15px', backgroundColor: '#10B981', color: 'white', border: 'none', borderRadius: '5px' }}
                >
                    새 문의 작성하기
                </button>
            </div>

            {inquiries.length === 0 ? (
                <p style={{ textAlign: 'center', padding: '50px', border: '1px solid #eee' }}>
                    작성된 문의 내역이 없습니다.
                </p>
            ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ backgroundColor: '#f5f5f5' }}>
                            <th style={tableHeaderStyle}>번호</th>
                            <th style={tableHeaderStyle}>제목</th>
                            <th style={tableHeaderStyle}>작성일</th>
                            <th style={tableHeaderStyle}>상태</th>
                            <th style={tableHeaderStyle}>답변일</th>
                        </tr>
                    </thead>
                    <tbody>
                        {inquiries.map((inquiry) => (
                            <tr key={inquiry.id} style={tableRowStyle}>
                                <td style={tableCellStyle}>{inquiry.id}</td>
                                <td
                                    style={{ ...tableCellStyle, textAlign: 'left', cursor: 'pointer', fontWeight: 'bold' }}
                                // 상세 페이지가 있다면 navigate(`/inquiry/${inquiry.id}`)
                                >
                                    {inquiry.title}
                                </td>
                                <td style={tableCellStyle}>{new Date(inquiry.createdAt).toLocaleDateString()}</td>
                                <td style={{ ...tableCellStyle, color: inquiry.answeredAt ? '#10B981' : '#ff9800', fontWeight: 'bold' }}>
                                    {getStatusText(inquiry.answeredAt)}
                                </td>
                                <td style={tableCellStyle}>
                                    {inquiry.answeredAt ? new Date(inquiry.answeredAt).toLocaleDateString() : '-'}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

const tableHeaderStyle = { padding: '10px', border: '1px solid #ddd' };
const tableCellStyle = { padding: '10px', border: '1px solid #eee', textAlign: 'center' };
const tableRowStyle = { transition: 'background-color 0.3s' };

export default MyInquiries;