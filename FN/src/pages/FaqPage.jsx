import React, { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../css/FaqPage.scss";

// 질문 목록
const faqData = [
    { id: 1, category: "회원", question: "회원가입은 어떻게 하나요?", answer: "로그인 페이지 하단의 '회원가입'을 통해 가입하실 수 있습니다." },
    { id: 2, category: "회원", question: "비밀번호를 잊어버렸어요.", answer: "로그인 페이지 하단의 '비밀번호 찾기'를 통해 재설정할 수 있습니다." },
    { id: 3, category: "서비스", question: "작성한 문의는 어디서 확인하나요?", answer: "고객센터 페이지 하단의 '나의 문의 목록'을 통해 확인 가능합니다." },
    { id: 4, category: "서비스", question: "답변은 얼마나 걸리나요?", answer: "평균적으로 1~2 영업일 이내에 답변드립니다." },
    { id: 5, category: "기타", question: "고객센터 이용시간은 어떻게 되나요?", answer: "고객센터는 평일 오전 9시부터 오후 6시까지 운영됩니다." },
    { id: 6, category: "기타", question: "점심메뉴 추천 해주세요", answer: "순대국밥·뼈해장국·콩나물국밥·김치찌개·된장찌개·부대찌개·김밥·라면·떡볶이·순대·튀김·어묵·돈까스·카레" },
    { id: 7, category: "기타", question: "수료날짜는 어떻게 되나요?", answer: "수료 날짜는 2025.12.05 입니다." },
];

function FaqPage() {
    const navigate = useNavigate();
    const [openId, setOpenId] = useState(null);
    const [selectedCategory, setSelectedCategory] = useState("전체");
    const [searchTerm, setSearchTerm] = useState("");

    const categories = ["전체", "회원", "서비스", "기타"];

    const toggleFaq = (id) => {
        setOpenId(openId === id ? null : id);
    };

    const filteredFaqs = faqData.filter((faq) => {
        const matchCategory = selectedCategory === "전체" || faq.category === selectedCategory;
        const matchSearch = faq.question.toLowerCase().includes(searchTerm.toLowerCase());
        return matchCategory && matchSearch;
    });

    const topFaqs = faqData.slice(0, 5);

    return (
        <div className="faq-container">
            <h2 className="faq-title">고객센터 (FAQ)</h2>

            <div className="faq-controls">
                <input
                    type="text"
                    placeholder="궁금한 내용을 검색해보세요."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="faq-search"
                />
                <div className="faq-categories">
                    {categories.map((cat) => (
                        <button
                            key={cat}
                            className={selectedCategory === cat ? "active" : ""}
                            onClick={() => setSelectedCategory(cat)}
                        >
                            {cat}
                        </button>
                    ))}
                </div>
            </div>

            <div className="top-faqs">
                <h3>
                    <span className="material-symbols-outlined">brand_awareness</span>
                    자주 묻는 질문
                </h3>
                <ul>
                    {topFaqs.map((faq) => (
                        <li key={faq.id} onClick={() => setOpenId(faq.id)}>
                            {faq.question}
                        </li>
                    ))}
                </ul>
            </div>


            <div className="faq-list">
                {filteredFaqs.map((faq) => (
                    <FaqItem
                        key={faq.id}
                        faq={faq}
                        open={openId === faq.id}
                        onToggle={() => toggleFaq(faq.id)}
                    />
                ))}
            </div>

            <div className="faq-footer">
                <button onClick={() => navigate("/inquiry/write")}>
                    1:1 문의 작성하기
                </button>

                <button onClick={() => navigate("/myprofile/inquiries")}>
                    나의 문의 목록
                </button>
            </div>

        </div>
    );
}

/* 개별 FAQ 아이템 (애니메이션 포함) */
function FaqItem({ faq, open, onToggle }) {
    const contentRef = useRef(null);
    const [height, setHeight] = useState("0px");

    useEffect(() => {
        setHeight(open ? `${contentRef.current.scrollHeight}px` : "0px");
    }, [open]);

    return (
        <div className="faq-item">
            <div className="faq-question" onClick={onToggle}>
                {faq.question}
                <span>{open ? "▲" : "▼"}</span>
            </div>
            <div
                ref={contentRef}
                className={`faq-answer-wrapper ${open ? "open" : ""}`}
                style={{ height }}
            >
                <div className="faq-answer">
                    <span className="material-symbols-outlined faq-icon">
                        brand_awareness
                    </span>
                    {faq.answer}
                </div>
            </div>
        </div>
    );
}

export default FaqPage;
