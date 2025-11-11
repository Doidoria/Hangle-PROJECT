import Layout from './Layout.jsx'
import { Link } from 'react-router-dom';
import '../css/setting.scss'

import { useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import { useAuth } from '../api/AuthContext';


const Setting = () => {

    //  상태 관리
    const { username: currentUsername } = useAuth(); // 현재 사용자 이름
    const [currentEmail, setCurrentEmail] = useState(''); // DB에서 불러올 현재 이메일
    const [newEmail, setNewEmail] = useState('');     // 사용자가 입력할 새 이메일
    const [isEditingEmail, setIsEditingEmail] = useState(false); // 이메일 변경 폼 표시 여부
    const [message, setMessage] = useState('');
    const [isError, setIsError] = useState(false);
    const { userId } = useAuth();

    // 이메일 변경 핸들러 (버튼: "저장" 클릭 시)
    const handleEmailChange = async () => {
        setMessage('');
        setIsError(false);

        // 1. 유효성 검사
        if (!newEmail || newEmail === currentEmail) {
            setMessage('새로운 이메일을 정확히 입력해주세요.');
            setIsError(true);
            return;
        }

        try {
            // 2. API 호출 (PUT /api/settings/email)
            const resp = await api.put('/api/settings/email', {
                newEmail: newEmail
            }); //  newEmail 필드명 일치 확인

            console.log("이메일 변경 성공:", resp.data);
            setMessage(resp.data || '이메일이 성공적으로 변경되었습니다.');
            setIsError(false);
            setCurrentEmail(newEmail); // UI 상의 현재 이메일 업데이트
            setIsEditingEmail(false); // 폼 닫기
        } catch (error) {
            console.error("이메일 변경 실패:", error);
            const errMsg = error.response?.data || '이메일 변경에 실패했습니다.';
            setMessage(errMsg);
            setIsError(true);
        }
    };

    return (
        <>
            <main className="main">
                <div className="title-wrap">
                    <h1>설정</h1>
                    <p style={{ fontWeight: 600 }}>
                        Hangle 계정 및 알림 모든 커뮤니케이션에 대한 제어
                    </p>
                </div>
                <section className="section-wrap">
                    <div className="tabs">
                        <Link to="#" className="tab-item active">
                            계정
                        </Link>
                        <Link to="#" className="tab-item">
                            알림
                        </Link>
                    </div>
                    <div className="setting-wrap">
                        {/* 계정 */}
                        <div className="settings-content">
                            <div className="info-group email-group">
                                <h2 className="group-title">귀하의 이메일 주소</h2>
                                {/* userId (이메일) 표시 */}
                                <p className="data-text">{userId || '로그인이 필요합니다.'}</p>
                                <button className="action-button">이메일 변경</button>
                                <p className="data-text">{currentEmail || "이메일 정보가 없습니다."}</p>

                                {/*  이메일 변경 폼 */}
                                {isEditingEmail ? (
                                    <div style={{ marginTop: '10px' }}>
                                        <input
                                            type="email"
                                            placeholder="새로운 이메일 주소"
                                            value={newEmail}
                                            onChange={e => setNewEmail(e.target.value)}
                                            className="input-field"
                                            style={{ marginBottom: '10px', padding: '10px', width: '80%' }}
                                        />
                                        <div style={{ display: 'flex', gap: '10px' }}>
                                            <button
                                                className="action-button primary-button"
                                                onClick={handleEmailChange} // 폼 저장 함수 연결
                                            >
                                                저장
                                            </button>
                                            <button
                                                className="action-button"
                                                onClick={() => {
                                                    setIsEditingEmail(false);
                                                    setNewEmail(''); // 입력 값 초기화
                                                    setMessage(''); // 메시지 초기화
                                                }}
                                            >
                                                취소
                                            </button>
                                        </div>
                                    </div>
                                ) : (
                                    <button
                                        className="action-button"
                                        onClick={() => setIsEditingEmail(true)} // 폼 열기
                                    >
                                        이메일 변경
                                    </button>
                                )}

                                {/*  메시지 표시 */}
                                {message && (
                                    <p style={{ color: isError ? 'red' : 'green', marginTop: '10px' }}>
                                        {message}
                                    </p>
                                )}
                            </div>
                            <hr className="divider" />
                            <div className="verification-section phone-verification">
                                <h2 className="section-title">전화 확인</h2>
                                <p className="section-description">
                                    계정이 인증되지 않았습니다. 전화번호로 계정을 인증하면
                                    Hangle에서 더 많은 활동을 할 수 있고, 스팸 및 기타 악용 사례를
                                    방지하는데 도움이 됩니다.
                                </p>
                                <button className="action-button primary-button">
                                    전화 확인
                                </button>
                            </div>
                            <hr className="divider" />
                            <div className="verification-section identity-verification">
                                <h2 className="section-title">신원 확인</h2>
                                <p className="section-description">
                                    신뢰할 수 있는 제3자 서비스인{" "}
                                    <strong className="highlight">Persona</strong>를 사용하여 신원
                                    확인을 확인하였습니다. 신원 확인을 통해 신원 확인이 필요한
                                    대회에 참가할 수 있습니다.
                                    <Link to="#" className="more-info-link">
                                        자세히 알아보기
                                    </Link>
                                </p>
                                <button className="action-button primary-button">
                                    내 계정을 확인하세요
                                </button>
                            </div>
                            <hr className="divider" />
                            <div className="theme-section verification-section">
                                <h2 className="section-title">주제</h2>
                                <p className="section-description">
                                    아래에서 Hangle UI 테마를 선택하세요.
                                </p>
                                <div className="custom-select-container" data-current-value="dark">
                                    <button className="select-display-button" aria-expanded="false" aria-controls="theme-options">
                                        <img src="./image/icon_moon(white).png" alt="다크" />
                                        <span className="selected-text">다크 테마</span>
                                        <span className="arrow-icon">▼</span>
                                    </button>
                                    <ul className="select-options-list" id="theme-options" role="listbox" style={{ display: "none" }}>
                                        <li data-value="dark" role="option" aria-selected="true" className="select-option active">
                                            <img src="./image/icon_moon(white).png" alt="다크" />
                                            <span>다크 테마</span>
                                        </li>
                                        <li data-value="light" role="option" aria-selected="false" className="select-option">
                                            <img src="./image/icon_sun(white).png" alt="라이트" />
                                            <span>라이트 테마</span>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                            <hr className="divider" />
                            <div className="delete-account-section verification-section">
                                <h2 className="section-title delete-title">Hangle 계정 삭제</h2>
                                <p className="section-description">
                                    Hangle 계정을 영구적으로 삭제하세요
                                </p>
                                <button className="action-button delete-button">계정 삭제</button>
                            </div>
                        </div>
                        {/* 알림 */}
                        <div className="notification-settings">
                            <div className="verification-section">
                                <h2 className="section-title">알림 설정</h2>
                                <p className="section-description">
                                    Hangle에서 수신하고 싶은 알림 종류를 선택하세요.
                                </p>
                                <div className="notification-options">
                                    <label className="noti-label">
                                        <input type="checkbox" defaultChecked="" />
                                        대회 업데이트 및 공지사항 이메일 수신
                                    </label>
                                    <label className="noti-label">
                                        <input type="checkbox" />
                                        새로운 커뮤니티 댓글 및 멘션 알림
                                    </label>
                                    <label className="noti-label">
                                        <input type="checkbox" defaultChecked="" />
                                        주요 플랫폼 변경 사항 알림
                                    </label>
                                </div>
                            </div>
                        </div>
                        {/* 사용자 이름 고정 */}
                        <div className="name-group">
                            <h2 className="group-title">사용자 이름</h2>
                            <p className="data-text">홍길동</p>
                            <a to="/" className="name-change">
                                이름 변경
                            </a>
                        </div>
                    </div>
                </section>
            </main>
        </>
    )
}

export default Setting