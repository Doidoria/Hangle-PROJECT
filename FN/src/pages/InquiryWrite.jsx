import React, { useState } from 'react';
import api from '../api/axiosConfig';
import { useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';
import '../css/InquiryWrite.scss'

function InquiryWrite() {
    const [formData, setFormData] = useState({
        title: '',
        content: ''
    });
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.title || !formData.content) {
            Swal.fire({
                icon: 'warning',
                title: '필수 입력',
                text: '제목과 내용을 모두 입력해 주세요.',
            });
            return;
        }

        try {
            // Spring Boot API 호출 (POST http://localhost:8090/api/inquiry)
            const response = await api.post('/api/inquiry', formData);

            Swal.fire({
                icon: 'success',
                title: '문의 접수 완료',
                text: `문의가 성공적으로 접수되었습니다. (접수번호: ${response.data.id})`,
                confirmButtonColor: '#10B981'
            });

            // 성공 후 마이페이지 문의 목록 페이지로 이동 (기존 myProfile 경로에 맞춤)
            navigate('/myprofile/inquiries');

        } catch (error) {
            // 인증 오류 (401 등)는 Axios 인터셉터가 처리하고 리다이렉트합니다.
            // 여기서는 통신 실패나 기타 서버 응답 오류만 처리합니다.
            console.error('문의 작성 실패:', error);
            Swal.fire({
                icon: 'error',
                title: '작성 실패',
                text: '문의 작성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
            });
        }
    };

    return (
        <div className="inquiry-write-container">
            {/* 고객센터로 이동 버튼 */}
            <button
                className="go-faq-btn"
                onClick={() => navigate("/FaqPage")}
            >
                <span class="material-symbols-outlined">arrow_left</span>
                <span class="material-symbols-outlined">support_agent</span>
            </button>
            <h2>1:1 문의 작성</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="title">제목</label>
                    <input
                        id="title"
                        type="text"
                        name="title"
                        placeholder="문의 제목을 입력하세요."
                        value={formData.title}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="content">내용</label>
                    <textarea
                        id="content"
                        name="content"
                        placeholder="문의 내용을 자세히 입력하세요."
                        rows="10"
                        value={formData.content}
                        onChange={handleChange}
                        required
                    ></textarea>
                </div>
                <button type="submit">문의 등록</button>
            </form>
        </div>
    );
}

export default InquiryWrite;