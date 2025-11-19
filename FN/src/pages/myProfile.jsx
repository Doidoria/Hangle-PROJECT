import { useAuth } from '../api/AuthContext';
import { useState, useEffect } from 'react';
import { Link, Navigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import '../css/myProfile.scss'


const MyProfile = () => {
    const { username: currentUsername, userid } = useAuth();
    const [joinDate, setJoinDate] = useState('');
    const [lastLogin, setLastLogin] = useState('');
    const [loading, setLoading] = useState(true);
    const [introduction, setIntroduction] = useState('');
    const [editing, setEditing] = useState(false);
    const [profileImage, setProfileImage] = useState('');

    const formatDate = (dateString, includeTime = false) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');

        if (!includeTime) {
            return `${y}.${m}.${d}`;
        }
        let hours = date.getHours();
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const ampm = hours >= 12 ? '오후' : '오전';
        hours = hours % 12 || 12; // 0시는 12시로 표시

        return `${y}.${m}.${d} ${ampm} ${hours}:${minutes}`;
    };

    useEffect(() => {
        const fetchProfileData = async () => {
            try {
                console.log("[MyProfile] 사용자 정보 요청 시작...");
                const res = await api.get('/api/user/me', { withCredentials: true });

                console.log("[MyProfile] 응답 수신:", res);

                if (res.status === 200 && res.data) {
                    setJoinDate(formatDate(res.data.createdAt) || '가입일 정보 없음');
                    setLastLogin(formatDate(res.data.lastLoginAt, true) || '최근 접속 정보 없음');
                    setIntroduction(res.data.introduction || '');

                    if (res.data.profileImageUrl) {
                        setProfileImage("http://localhost:8090" + res.data.profileImageUrl);
                    } else {
                        setProfileImage("/image/default-avatar.png");
                    }
                }
            } catch (err) {
                console.error('[MyProfile] 사용자 정보 불러오기 실패:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchProfileData();
    }, []);

    const handleProfileImageChange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        try {
            const res = await api.post('/api/user/profile-image', formData, {
                headers: { 'Content-Type': 'multipart/form-data' },
                withCredentials: true,
            });

            console.log("업로드 응답:", res.data);

            const imageUrl = res.data.profileImageUrl || res.data.imageUrl;
            if (imageUrl) {
                setProfileImage("http://localhost:8090" + imageUrl + "?t=" + new Date().getTime());
            } else {
                setProfileImage("/image/default-avatar.png");
            }
            const refresh = await api.get('/api/user/me', { withCredentials: true });
            console.log("새 사용자 정보:", refresh.data);
        } catch (error) {
            console.error("프로필 이미지 업로드 실패:", error);
            alert('프로필 이미지 업로드 중 오류가 발생했습니다.');
        }
    };

    if (loading) {
        return (
            <main className="main">
                <p style={{ textAlign: 'center', marginTop: '50px' }}>프로필 정보를 불러오는 중...</p>
            </main>
        );
    }

    return (
        <section className="myprofile-wrap">
            <div className="profile-header">
                <div className="profile-banner" />
                <div className="profile-info">
                    <div className="profile-avatar">
                        <img src={profileImage} alt="user avatar" className="avatar-img" />
                        {/* 프로필 변경 버튼 */}
                        <label htmlFor="profileUpload" className="profile-upload-btn">
                            프로필 변경
                        </label>
                        <input id="profileUpload" type="file" accept="image/*"
                            style={{ display: 'none' }} onChange={handleProfileImageChange} />
                    </div>
                    <div className="profile-text">
                        <h2 className="username">{currentUsername || ''}</h2>
                        <p className="user-meta">
                            {joinDate ? `${joinDate} 가입` : '가입일 정보 없음'} ·{' '}
                            {lastLogin ? `최근 접속 ${lastLogin}` : '최근 접속 정보 없음'}
                        </p>
                        <p className="user-id">{userid}</p>
                    </div>
                    <div className="setting-menu">
                        <Link to="/setting" className="menu-item">
                            <span className="material-symbols-outlined gle-icon">settings</span>
                            <span>설정</span>
                        </Link>
                    </div>
                </div>
            </div>
            {/* ===== 정보 영역 ===== */}
            <div className="profile-body">
                <section className="info-section">
                    <h3>자기 소개</h3>
                    <p className="info-text">
                        {editing ? (<textarea value={introduction} onChange={(e) => setIntroduction(e.target.value)}
                            className="intro-textarea" rows="4" />
                        ) : (
                            <p className="info-text">{introduction || '아직 자기소개가 없습니다.'}</p>)}
                    </p>
                    <div className="profile-actions">
                        {editing ? (
                            <>
                                <button className="follow-btn" onClick={async () => {
                                    try {
                                        const resp = await api.put('/api/user/introduction', { introduction });
                                        console.log('자기소개 수정 완료:', resp.data);
                                        setEditing(false);
                                    } catch (err) {
                                        console.error('자기소개 수정 실패:', err);
                                    }
                                }}>저장
                                </button>
                                <button className="follow-btn" onClick={() => setEditing(false)}>취소</button>
                            </>) : (
                            <button className="follow-btn" onClick={() => setEditing(true)}>변경</button>
                        )}
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
        </section>
    )
}

export default MyProfile