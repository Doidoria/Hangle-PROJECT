import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // 1:1 문의 버튼을 위해 추가

// 더미 FAQ 데이터
const faqData = [
    { id: 1, question: "회원가입은 어떻게 하나요?", answer: "페이지 상단의 '회원가입' 버튼을 클릭하여 소셜 로그인 또는 이메일로 가입할 수 있습니다." },
    { id: 2, question: "비밀번호를 잊어버렸어요.", answer: "로그인 페이지 하단의 '비밀번호 찾기'를 통해 이메일 인증 후 재설정할 수 있습니다." },
    { id: 3, question: "작성한 문의는 어디서 확인하나요?", answer: "마이페이지 > 1:1 문의 내역에서 확인 가능하며, 답변이 완료되면 상태가 변경됩니다." },
];

function FaqPage() {
    const [openId, setOpenId] = useState(null);
    const navigate = useNavigate();

    const toggleFaq = (id) => {
        setOpenId(openId === id ? null : id);
    };

    return (
        <div className="faq-container">
            <h2>고객센터 (FAQ)</h2>
            {/* ... (FAQ 리스트 코드) ... */}
            <div className="faq-list">
                {faqData.map((faq) => (
                    <div key={faq.id} className="faq-item">
                        <div
                            className="faq-question"
                            onClick={() => toggleFaq(faq.id)}
                            style={{ cursor: 'pointer', fontWeight: 'bold', padding: '10px', borderBottom: '1px solid #eee' }}
                        >
                            {faq.question}
                            <span>{openId === faq.id ? ' ▲' : ' ▼'}</span>
                        </div>
                        {openId === faq.id && (
                            <div
                                className="faq-answer"
                                style={{ padding: '10px 20px', background: '#f9f9f9' }}
                            >
                                {faq.answer}
                            </div>
                        )}
                    </div>
                ))}
            </div>

            <div style={{ marginTop: '30px', textAlign: 'center' }}>
                <button onClick={() => navigate('/inquiry/write')} style={{ padding: '10px 20px', fontSize: '16px' }}>
                    1:1 문의 작성하기
                </button>
            </div>
        </div>
    );
}

export default FaqPage;