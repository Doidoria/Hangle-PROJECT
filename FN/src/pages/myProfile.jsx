import { useAuth } from '../api/AuthContext';
import { useState, useEffect } from 'react';
import { Link, Navigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import '../css/myProfile.scss'

const DEFAULT_AVATAR = '/image/default-avatar.png';

const normalizeProfile = (v) => {
    if (!v) return DEFAULT_AVATAR;
    if (v === 'null' || v === 'undefined') return DEFAULT_AVATAR;
    return v;
};

const MyProfile = () => {
    const { username: currentUsername, userid, profileImage, setProfileImage } = useAuth();
    const [joinDate, setJoinDate] = useState('');
    const [lastLogin, setLastLogin] = useState('');
    const [loading, setLoading] = useState(true);
    const [introduction, setIntroduction] = useState('');
    const [editing, setEditing] = useState(false);
    const [originalIntroduction, setOriginalIntroduction] = useState(''); 

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
            console.log('업로드 응답:', res.data);

            const imageUrl = res.data.profileImageUrl;
            if (imageUrl && imageUrl !== 'null') {
                const fullUrl = 'http://localhost:8090' + imageUrl + '?t=' + Date.now();
                setProfileImage(fullUrl);
                localStorage.setItem('profileImage', fullUrl);
            } else {
                setProfileImage(DEFAULT_AVATAR);
                localStorage.setItem('profileImage', DEFAULT_AVATAR);
            }
        } catch (error) {
            console.error('프로필 이미지 업로드 실패:', error);
            alert('프로필 이미지 업로드 중 오류가 발생했습니다.');
        }
    };

    const saveIntro = async () => {
        try {
            // PUT 요청으로 introduction 상태 값을 백엔드에 전송
            await api.put('/api/user/introduction', { introduction }, { withCredentials: true });
            setEditing(false);
            setOriginalIntroduction(introduction);
        } catch (error) {
            console.error('자기소개 저장 실패:', error);
            alert('자기소개 저장 중 오류가 발생했습니다.');
        }
    };

    // cancelEdit 함수 추가
    const cancelEdit = () => {
        setIntroduction(originalIntroduction); // 원본 값으로 되돌림
        setEditing(false);
    }

    useEffect(() => {
        const fetchProfileData = async () => {
            try {
                console.log("[MyProfile] 사용자 정보 요청 시작...");
                const res = await api.get('/api/user/me', { withCredentials: true });
                console.log("[MyProfile] 응답 수신:", res);

                if (res.status === 200 && res.data) {
                    setJoinDate(formatDate(res.data.createdAt) || '가입일 정보 없음');
                    setLastLogin(formatDate(res.data.lastLoginAt, true) || '최근 접속 정보 없음');
                    const introData = res.data.introduction || '';
                    setIntroduction(introData);
                    setOriginalIntroduction(introData);

                    const imageUrl = res.data.profileImageUrl;
                    if (imageUrl && imageUrl !== 'null') {
                        const fullUrl = 'http://localhost:8090' + imageUrl + '?t=' + Date.now();
                        setProfileImage(fullUrl);
                        localStorage.setItem('profileImage', fullUrl);
                    } else {
                        const safe = normalizeProfile(profileImage);
                        setProfileImage(safe);
                        localStorage.setItem('profileImage', safe);
                    }
                }
            } catch (err) {
                console.error('[MyProfile] 사용자 정보 불러오기 실패:', err);
                setProfileImage(DEFAULT_AVATAR);
                localStorage.setItem('profileImage', DEFAULT_AVATAR);
            } finally {
                setLoading(false);
            }
        };

        fetchProfileData();
    }, []);

    if (loading) {
        return (
            <main className="main">
                <p style={{ textAlign: 'center', marginTop: '50px' }}>프로필 정보를 불러오는 중...</p>
            </main>
        );
    }

    return (
        <section className="profile-page">
            <div className="profile-header">
                <div className="banner"></div>
                <div className="profile-main">
                    <div className="avatar-wrap">
                        <img src={normalizeProfile(profileImage)}
                            onError={(e) => (e.currentTarget.src = DEFAULT_AVATAR)} className="avatar-img"/>
                        <label htmlFor="profileUpload" className="change-photo">사진 변경</label>
                        <input id="profileUpload" type="file" onChange={handleProfileImageChange} hidden />
                    </div>
                    <div className="info">
                        <h2>{currentUsername}</h2>
                        <p className="meta">{joinDate} 가입 · 최근 접속 {lastLogin}</p>
                        <Link to="/setting" className="setting-btn">
                            <span className="material-symbols-outlined">settings</span> 설정
                        </Link>
                    </div>
                </div>
                {/* 하단 간단한 스탯 */}
                <div className="profile-stats">
                    <div className="stat">
                        <strong>5</strong>
                        <span>참여 대회</span>
                    </div>
                    <div className="stat">
                        <strong>12</strong>
                        <span>제출 기록</span>
                    </div>
                    <div className="stat">
                        <strong>3</strong>
                        <span>획득 뱃지</span>
                    </div>
                </div>
            </div>

            <div className="profile-grid">
                {/* 자기소개 카드 */}
                <section className="card">
                    <h3>자기소개</h3>
                    {!editing ? (
                        <p className="intro-text">{introduction || '아직 자기소개가 없습니다.'}</p>
                    ) : (
                        <textarea
                            className="intro-edit"
                            rows="4"
                            value={introduction}
                            onChange={(e) => setIntroduction(e.target.value)}/>
                    )}
                    <div className="action">
                        {!editing ? (
                            <button onClick={() => setEditing(true)}>수정하기</button>
                        ) : (
                            <>
                                <button onClick={saveIntro} className="primary">저장</button>
                                <button onClick={cancelEdit}>취소</button>
                            </>
                        )}
                    </div>
                </section>
                {/* 뱃지 카드 */}
                <section className="card badge-wrap">
                    <h3>보유 뱃지</h3>
                    <div className="badge-list">
                        <img width="48" height="48" src="https://img.icons8.com/color/48/trophy.png" alt="trophy"/>
                        <img src="https://cdn-icons-png.flaticon.com/512/3702/3702744.png" alt="뱃지2" />
                        <img src="https://cdn-icons-png.flaticon.com/512/3702/3702744.png" alt="뱃지3" />
                    </div>
                </section>
                {/* 활동 기록 카드 (예시) */}
                <section className="card full-width">
                    <h3>최근 활동 기록</h3>
                    <ul className="activity-list">
                        <li>2025.11.10 '지구 구하기' 대회 최종 제출</li>
                        <li>2025.11.05 '데이터 분석 챌린지' 대회 3위 달성</li>
                        <li>2025.10.28 '새로운 알고리즘' 게시글 작성</li>
                    </ul>
                </section>
            </div>
        </section>
    )
}

export default MyProfile