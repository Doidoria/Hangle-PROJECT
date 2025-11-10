import { useState } from 'react';
import { Link } from 'react-router-dom';
import Swal from 'sweetalert2';
import '../css/setting.scss';
import api from '../api/axiosConfig';
import { useAuth } from '../api/AuthContext';
import { useTheme } from '../api/ThemeContext';

const Setting = () => {
  const { username: currentUsername, userId } = useAuth();
  const [activeTab, setActiveTab] = useState('account');
  const [currentEmail, setCurrentEmail] = useState('');
  const [newEmail, setNewEmail] = useState('');
  const [isEditingEmail, setIsEditingEmail] = useState(false);
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const { theme, setTheme } = useTheme();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const [isVerified, setIsVerified] = useState(false);

  const handleTabClick = (tab) => setActiveTab(tab);

  const handleEmailChange = async () => {
    if (!isVerified) {
      Swal.fire({
        icon: 'warning',
        title: '본인 인증 필요',
        text: '본인 인증 후 회원정보를 변경할 수 있습니다.',
        confirmButtonColor: '#10B981',
      });
      return;
    }
    setMessage('');
    setIsError(false);

    if (!newEmail || newEmail === currentEmail) {
      setMessage('새로운 이메일을 정확히 입력해주세요.');
      setIsError(true);
      return;
    }

    try {
      const resp = await api.put('/api/settings/email', { newEmail });
      setMessage(resp.data || '이메일이 성공적으로 변경되었습니다.');
      setCurrentEmail(newEmail);
      setIsEditingEmail(false);

      Swal.fire({
        icon: 'success',
        title: '이메일 변경 완료',
        text: '이메일이 성공적으로 변경되었습니다.',
        confirmButtonColor: '#10B981',
      });
    } catch (error) {
      const errMsg = error.response?.data || '이메일 변경에 실패했습니다.';
      setMessage(errMsg);
      setIsError(true);
    }
  };

  const handleThemeSelect = (value) => {
    setTheme(value);
    setIsDropdownOpen(false);
    console.log(`테마가 ${value}로 변경되었습니다.`);
  };

  // 본인 인증 (임시 모의용)
  const handleVerification = async () => {
    const result = await Swal.fire({
      icon: 'question',
      title: '본인 인증',
      text: '휴대폰 본인 인증을 진행하시겠습니까?',
      showCancelButton: true,
      confirmButtonText: '진행하기',
      cancelButtonText: '취소',
      confirmButtonColor: '#10B981',
    });

    if (result.isConfirmed) {
      setIsVerified(true);
      Swal.fire({
        icon: 'success',
        title: '인증 완료',
        text: '본인 인증이 성공적으로 완료되었습니다.',
        confirmButtonColor: '#10B981',
      });
    }
  };

  const handleDeleteAccount = async () => {
    if (!isVerified) {
      Swal.fire({
        icon: 'warning',
        title: '본인 인증 필요',
        text: '본인 인증 후 계정을 삭제할 수 있습니다.',
        confirmButtonColor: '#10B981',
      });
      return;
    }

    const result = await Swal.fire({
      icon: 'warning',
      title: '정말 계정을 삭제하시겠습니까?',
      text: '삭제 후에는 복구할 수 없습니다.',
      showCancelButton: true,
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      confirmButtonColor: '#d33',
    });

    if (result.isConfirmed) {
      Swal.fire({
        icon: 'success',
        title: '계정 삭제 완료',
        text: '계정이 정상적으로 삭제되었습니다.',
        confirmButtonColor: '#10B981',
      });
    }
  };

  return (
    <main className="main">
      <div className="title-wrap">
        <h1>설정</h1>
        <p style={{ fontWeight: 600 }}>Hangle 계정 및 알림 모든 커뮤니케이션에 대한 제어</p>
      </div>
      <section className="section-wrap">
        {/* Tabs */}
        <div className="tabs">
          <button
            className={`tab-item ${activeTab === 'account' ? 'active' : ''}`}
            onClick={() => handleTabClick('account')}>계정
          </button>
          <button
            className={`tab-item ${activeTab === 'notification' ? 'active' : ''}`}
            onClick={() => handleTabClick('notification')}>알림
          </button>
        </div>
        <div className="setting-wrap">
          {/* 오른쪽 고정 이름 섹션 */}
          <div className="name-group">
            <h2 className="group-title">사용자 이름</h2>
            <p className="data-text">{currentUsername || ''}</p>
            <Link to="#" className={`name-change ${!isVerified ? 'disabled' : ''}`}
              onClick={(e) => {
                if (!isVerified) {
                  e.preventDefault();
                  Swal.fire({
                    icon: 'warning',
                    title: '본인 인증 필요',
                    text: '본인 인증 후 회원정보를 변경할 수 있습니다.',
                    confirmButtonColor: '#10B981',
                    });
                  }
                }}>
              이름 변경
            </Link>
          </div>
          {/* 계정 탭 */}
          {activeTab === 'account' && (
            <div className="settings-content">
              {/* 이메일 변경 */}
              <div className="info-group email-group">
                <h2 className="group-title">귀하의 이메일 주소</h2>
                <p className="data-text">{userId || '로그인이 필요합니다.'}</p>
                {isEditingEmail ? (
                  <div style={{ marginTop: '10px' }}>
                    <input type="email" placeholder="새로운 이메일 주소" value={newEmail}
                      onChange={(e) => setNewEmail(e.target.value)} className="input-field"
                      style={{ marginBottom: '10px', padding: '10px', width: '80%' }}/>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <button className="action-button primary-button" onClick={handleEmailChange}>
                        저장
                      </button>
                      <button className="action-button" onClick={() => { setIsEditingEmail(false);
                          setNewEmail(''); setMessage('');}}>
                        취소
                      </button>
                    </div>
                  </div>
                ) : (
                  <button className="action-button" disabled={!isVerified}
                    onClick={() => { if (!isVerified) { Swal.fire({
                          icon: 'warning',
                          title: '본인 인증 필요',
                          text: '본인 인증 후 이메일 변경이 가능합니다.',
                          confirmButtonColor: '#10B981',
                        });
                        return;} setIsEditingEmail(true);}}>
                    이메일 변경
                  </button>
                )}
                {message && (<p style={{ color: isError ? 'red' : 'green', marginTop: '10px' }}>{message}</p>)}
              </div>
              <hr className="divider" />
              {/* 본인 인증 */}
              <div className="verification-section phone-verification">
                <h2 className="section-title">본인 확인</h2>
                <p className="section-description"> {isVerified
                    ? '본인 인증이 완료되었습니다'
                    : '계정이 인증되지 않았습니다. 전화번호로 인증을 완료해주세요.'}
                </p>
                <button className={`action-button primary-button ${isVerified ? 'verified' : ''}`}
                  onClick={handleVerification} disabled={isVerified}>
                  {isVerified ? '인증 완료' : '본인 인증'}
                </button>
              </div>
              <hr className="divider" />
              {/* 테마 선택 */}
              <div className="theme-section verification-section">
                <h2 className="section-title">테마 변경</h2>
                <p className="section-description">아래에서 Hangle UI 테마를 선택하세요.</p>
                <div className="custom-select-container" data-current-value={theme}>
                  <button className="select-display-button" aria-expanded={isDropdownOpen}
                    onClick={() => setIsDropdownOpen((prev) => !prev)}>
                    <img src={theme === 'dark' ? './image/icon_moon(white).png' : './image/icon_sun(black).png'}
                      alt={theme === 'dark' ? '다크' : '라이트'} />
                    <span className="selected-text">
                      {theme === 'dark' ? '다크 테마' : '라이트 테마'}
                    </span>
                    <span className="arrow-icon">▼</span>
                  </button>
                  {isDropdownOpen && (
                    <ul className="select-options-list">
                      <li className={`select-option ${theme === 'dark' ? 'active' : ''}`}
                        onClick={() => handleThemeSelect('dark')}>
                        <img src={theme === 'light'
                          ? './image/icon_moon(black).png'
                          : './image/icon_moon(white).png'} alt="다크" />
                        <span>다크 테마</span>
                      </li>
                      <li className={`select-option ${theme === 'light' ? 'active' : ''}`}
                        onClick={() => handleThemeSelect('light')}>
                        <img src={theme === 'light'
                          ? './image/icon_sun(black).png'
                          : './image/icon_sun(white).png'} alt="라이트" />
                        <span>라이트 테마</span>
                      </li>
                    </ul>
                  )}
                </div>
              </div>
              <hr className="divider" />
              {/* 계정 삭제 */}
              <div className="delete-account-section verification-section">
                <h2 className="section-title delete-title">Hangle 계정 삭제</h2>
                <p className="section-description">Hangle 계정을 영구적으로 삭제하세요.</p>
                <button className="action-button delete-button" 
                  disabled={!isVerified} onClick={handleDeleteAccount}>
                  계정 삭제
                </button>
              </div>
            </div>
          )}
          {/* 알림 탭 */}
          {activeTab === 'notification' && (
            <div className="notification-settings">
              <div className="verification-section">
                <h2 className="section-title">알림 설정</h2>
                <p className="section-description">Hangle에서 수신하고 싶은 알림 종류를 선택하세요.</p>
                <div className="notification-options">
                  <label className="noti-label">
                    <input type="checkbox" defaultChecked /> 대회 업데이트 및 공지사항 이메일 수신
                  </label>
                  <label className="noti-label">
                    <input type="checkbox" /> 새로운 커뮤니티 댓글 및 멘션 알림
                  </label>
                  <label className="noti-label">
                    <input type="checkbox" defaultChecked /> 주요 플랫폼 변경 사항 알림
                  </label>
                </div>
              </div>
            </div>
          )}
        </div>
      </section>
    </main>
  );
};

export default Setting;
