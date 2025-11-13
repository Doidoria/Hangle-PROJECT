import Layout from './Layout.jsx'
import { Link } from 'react-router-dom';
import '../css/myProfile.scss'


const MyProfile = () => {

    return (
        <>
            <main className="main">
                {/* ===== 프로필 상단 버튼 바 ===== */}
                <div className="profile-topbar">
                    <div className="profile-menu">
                        <div className="menu-item-wrap">
                            <Link to="/setting" className="menu-item">
                                <span className="material-symbols-outlined gle-icon">
                                    settings
                                </span>
                                <span>Settings</span>
                            </Link>
                        </div>
                        <Link to="#" className="menu-item">
                            <span className="material-symbols-outlined">Work</span>
                            <span>Your Work</span>
                        </Link>
                        <Link to="#" className="menu-item">
                            <span className="material-symbols-outlined">trending_up</span>
                            <span>Progression</span>
                        </Link>
                    </div>
                </div>
                <div className="profile-header">
                    <div className="profile-banner" />
                    <div className="profile-info">
                        <div className="profile-avatar">
                            <img
                                src="https://cdn-icons-png.flaticon.com/512/5997/5997002.png"
                                alt="user avatar"
                            />
                        </div>
                        <div className="profile-text">
                            <h2 className="username">전익환</h2>
                            <p className="user-meta">2일 전에 가입함 · 최근 접속 1일 전</p>
                        </div>
                        <div className="profile-actions">
                            <button className="follow-btn">팔로우</button>
                            <button className="contact-btn">메시지</button>
                        </div>
                    </div>
                </div>
                {/* ===== 정보 영역 ===== */}
                <div className="profile-body">
                    <section className="info-section">
                        <h3>정보</h3>
                        <p className="info-text">아직 자기소개가 없습니다.</p>
                        <p className="info-sub">작업 중...</p>
                        <div className="profile-actions">
                            <button className="follow-btn">변경</button>
                            <button className="contact-btn">저장</button>
                        </div>
                    </section>
                    {/* ===== 뱃지 영역 ===== */}
                    <section className="badge-section">
                        <h3>뱃지</h3>
                        <div className="badges">
                            <div className="badge">
                                <img
                                    src="https://cdn-icons-png.flaticon.com/512/3702/3702744.png"
                                    alt="Badge"
                                />
                            </div>
                        </div>
                    </section>
                </div>
            </main>
        </ >
    )
}

export default MyProfile