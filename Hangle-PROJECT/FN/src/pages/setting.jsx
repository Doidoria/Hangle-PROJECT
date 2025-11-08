import Layout from './Layout.jsx'
import { Link } from 'react-router-dom';
import { useEffect, useState } from "react";
import '../css/setting.scss'

const Setting = () => {

    return (
        <Layout>
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
                            <div className="info-group-container">
                                <div className="info-group email-group">
                                    <h2 className="group-title">귀하의 이메일 주소</h2>
                                    <p className="data-text">유저 아이디</p>
                                    <button className="action-button">이메일 변경</button>
                                </div>
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
        </Layout>
    )
}

export default Setting