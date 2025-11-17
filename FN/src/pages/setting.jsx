import '../css/setting.scss';
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Swal from 'sweetalert2';
import api from '../api/axiosConfig';
import { useAuth } from '../api/AuthContext';
import { useTheme } from '../api/ThemeContext';

const PORTONE_STORE_ID = 'imp78416545';
const PORTONE_CHANNEL_KEY = 'channel-key-7e34fb1c-d765-46d6-9f5e-f76dfbffd370';

const Setting = () => {
  const [activeTab, setActiveTab] = useState('account');
  const { username: currentUsername, userid, setUserid, isLoading } = useAuth();
  const [currentEmail, setCurrentEmail] = useState(userid || '');
  const [newEmail, setNewEmail] = useState('');
  const [isEditingEmail, setIsEditingEmail] = useState(false);
  const [newUsername, setNewUsername] = useState('');
  const [isEditingUsername, setIsEditingUsername] = useState(false);
  const { theme, setTheme } = useTheme();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isVerified, setIsVerified] = useState(false);
  const handleTabClick = (tab) => setActiveTab(tab);
  const [isEditingPassword, setIsEditingPassword] = useState(false);
  const [currentPw, setCurrentPw] = useState('');
  const [newPw, setNewPw] = useState('');
  const [confirmPw, setConfirmPw] = useState('');

  useEffect(() => {
    if (userid) setCurrentEmail(userid);
    if (window.IMP) {
      window.IMP.init(PORTONE_STORE_ID);
    }
  }, [userid]);

  // 이름 변경
  const handleUsernameChange = async () => {
    if (!isVerified) {
      Swal.fire({
        icon: 'warning', title: '본인 인증 필요', text: '본인 인증 후 회원정보를 변경할 수 있습니다.',
        confirmButtonColor: '#10B981',
      });
      return;
    }

    if (!newUsername || newUsername === currentUsername) {
      Swal.fire({
        icon: 'info', title: '변경할 이름을 입력해주세요.',
        confirmButtonColor: '#10B981',
      });
      return;
    }

    try {
      const resp = await api.put('/api/user/update-info', { username: newUsername });
      Swal.fire({
        icon: 'success', title: '이름 변경 완료', text: '이름이 성공적으로 변경되었습니다.',
        confirmButtonColor: '#10B981',
      });
      setNewUsername('');
      setIsEditingUsername(false);
    } catch (error) {
      Swal.fire({
        icon: 'error', title: '이름 변경 실패', text: error.response?.data?.error || '서버 오류가 발생했습니다.',
        confirmButtonColor: '#d33',
      });
    }
  };

  // 이메일(userid) 변경
  const handleEmailChange = async () => {
    if (!isVerified) {
      Swal.fire({
        icon: 'warning', title: '본인 인증 필요', text: '본인 인증 후 회원정보를 변경할 수 있습니다.',
        confirmButtonColor: '#10B981',
      });
      return;
    }

    if (!newEmail || newEmail === currentEmail) {
      Swal.fire({
        icon: 'info', title: '변경할 이메일을 입력해주세요.',
        confirmButtonColor: '#10B981',
      });
      return;
    }

    try {
      const resp = await api.put('/api/user/update-info', { userid: newEmail });
      if (resp.data.message?.includes('로그아웃')) {
        Swal.fire({
          icon: 'success', title: '이메일 변경 완료',
          text: '이메일이 변경되어 로그아웃되었습니다. 다시 로그인해주세요.',
          confirmButtonColor: '#10B981',
        }).then(() => {
          // 쿠키 삭제 (클라이언트 쪽에서도 확실히 정리)
          document.cookie = "access-token=; path=/; max-age=0; sameSite=None; secure=false";
          document.cookie = "userid=; path=/; max-age=0; sameSite=None; secure=false";
          window.location.href = '/login';
        });
      }
      // 단순 이메일만 수정(로그아웃 아님)
      else {
        Swal.fire({
          icon: 'success', title: '이메일 변경 완료', text: '이메일이 성공적으로 변경되었습니다.',
          confirmButtonColor: '#10B981',
        });
        setCurrentEmail(newEmail);
        setUserid(newEmail);
        localStorage.setItem("userid", newEmail);
        setNewEmail('');
        setIsEditingEmail(false);
      }
    } catch (error) {
      Swal.fire({
        icon: 'error', title: '이메일 변경 실패',
        text: error.response?.data?.error || '서버 오류가 발생했습니다.',
        confirmButtonColor: '#d33',
      });
    }
  };

  // 비밀번호 변경
  const handlePasswordChange = async () => {
    if (!isVerified) {
      Swal.fire({
        icon: "warning", title: "본인 인증 필요",
        text: "본인 인증 후 비밀번호 변경이 가능합니다.",
        confirmButtonColor: "#10B981",
      });
      return;
    }

    if (!currentPw || !newPw || !confirmPw) {
      Swal.fire({ icon: "info", title: "모든 입력값을 입력해주세요." });
      return;
    }

    try {
      const resp = await api.put("/api/user/change-password", {
        currentPassword: currentPw,
        newPassword: newPw,
        confirmPassword: confirmPw,
      });
      Swal.fire({
        icon: "success", title: "비밀번호 변경 완료",
        text: "다시 로그인해주세요.",
      }).then(() => {
        // 클라이언트 쿠키 정리
        document.cookie = "access-token=; path=/; max-age=0;";
        document.cookie = "userid=; path=/; max-age=0;";
        localStorage.clear();
        window.location.href = "/login";
      });

    } catch (error) {
      Swal.fire({
        icon: "error", title: "비밀번호 변경 실패",
        text: error.response?.data?.error || "서버 오류가 발생했습니다.",
      });
    }
  };

  // 휴대폰 인증
  // const handlePhoneVerification = () => {
  //   const IMP = window.IMP;

  //   if (!IMP) {
  //     alert("PortOne SDK 초기화 실패. index.html 확인 필요.");
  //     return;
  //   }
  //   const merchant_uid = `cert_${new Date().getTime()}`;

  //   IMP.certification(
  //     {
  //       channelKey: PORTONE_CHANNEL_KEY,
  //       merchant_uid: merchant_uid,
  //       popup: false,
  //     },
  //     async (resp) => {
  //       if (resp.success) {
  //         const imp_uid = resp.imp_uid;

  //         try {
  //           // 백엔드 검증 API 호출
  //           const serverResponse = await api.get(`/portOne/certifications/${imp_uid}`);

  //           if (serverResponse.data.isVerified) {
  //             Swal.fire({
  //               icon: 'success', title: '인증 완료',
  //               text: "본인 인증 및 휴대폰 번호 확인이 완료되었습니다. 회원정보를 변경할 수 있습니다.",
  //             });
  //             setIsVerified(true);
  //           } else {
  //             Swal.fire({
  //               icon: 'error', title: '인증 실패',
  //               text: serverResponse.data.message,
  //             });
  //             setIsVerified(false);
  //           }

  //         } catch (error) {
  //           console.error("백엔드 검증 중 오류 발생:", error);
  //           alert("서버 통신 오류가 발생했습니다.");
  //         }

  //       } else {
  //         Swal.fire({
  //           icon: 'warning', title: '인증 취소/실패',
  //           text: resp.error_msg,
  //         });
  //       }
  //     }
  //   );
  // };

  // 개발용 휴대폰 인증 처리
  const handlePhoneVerification = () => {
    setIsVerified(true);
    Swal.fire({
      icon: "success",
      title: "개발 모드",
      text: "본인 인증이 완료되었습니다.",
      confirmButtonColor: "#10B981",
    });
  };


  // 계정 삭제
  const handleDeleteAccount = async () => {
    if (!isVerified) {
      Swal.fire({
        icon: 'warning', title: '본인 인증 필요',
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
      try {
        await api.delete('/api/user/delete');
        Swal.fire({
          icon: 'success', title: '계정 삭제 완료', text: '계정이 정상적으로 삭제되었습니다.',
          confirmButtonColor: '#10B981',
        }).then(() => {
          localStorage.clear();
          window.location.href = '/';
        });
      } catch (error) {
        Swal.fire({
          icon: 'error', title: '삭제 실패', text: '계정을 삭제하는 중 문제가 발생했습니다.',
          confirmButtonColor: '#d33',
        });
      }
    }
  };

  // 테마 저장
  const handleThemeSelect = async (value) => {
    setTheme(value);
    setIsDropdownOpen(false);

    try {
      await api.put("/api/user/theme", { theme: value });
      localStorage.setItem("theme", value);
    } catch (err) {
      console.error("테마 저장 실패:", err);
    }
  };

  return (
    <section className="setting-wrap">
      <div className="title-wrap">
        <h1>설정</h1>
        <p style={{ fontWeight: 600 }}>Hangle 계정 및 알림 모든 커뮤니케이션에 대한 제어</p>
      </div>
      <div className="section-wrap">
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
        <div className="setting-inner">
          {/* 사용자 이름 변경 섹션 */}
          <div className="name-group">
            <h2 className="group-title">사용자 이름</h2>
            <p className="data-text">{currentUsername || ''}</p>
            {isEditingUsername ? (
              <div className="edit-form-container">
                <input type="text" placeholder="새로운 이름을 입력하세요" value={newUsername}
                  onChange={(e) => setNewUsername(e.target.value)} className="input-field" />
                <div className="action-buttons-group">
                  <button className="action-button primary-button" onClick={handleUsernameChange}>저장</button>
                  <button className="action-button" onClick={() => { setIsEditingUsername(false); setNewUsername(''); }}>취소</button>
                </div>
              </div>
            ) : (
              <Link to="#" className={`name-change ${!isVerified ? 'disabled' : ''}`}
                onClick={(e) => {
                  e.preventDefault();
                  if (!isVerified) {
                    Swal.fire({
                      icon: 'warning', title: '본인 인증 필요',
                      text: '본인 인증 후 이름 변경이 가능합니다.',
                      confirmButtonColor: '#10B981',
                    }); return;
                  } setIsEditingUsername(true);
                }}> 이름 변경
              </Link>
            )}
          </div>
          <hr className="divider" />
          {/* 이메일(userid) 변경 섹션 */}
          {activeTab === 'account' && (
            <div className="settings-content">
              <div className='setting-card'>
                <div className="info-group email-group">
                  <h2 className="group-title">귀하의 이메일 주소</h2>
                  <p className="data-text">{userid ? currentEmail : ""}</p>
                  {isEditingEmail ? (<div style={{ marginTop: '10px' }}>
                    <input type="email" placeholder="새로운 이메일 주소를 입력하세요" value={newEmail}
                      onChange={(e) => setNewEmail(e.target.value)} className="input-field"
                      style={{ marginBottom: '15px', padding: '10px', width: '80%' }} />
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <button className="action-button primary-button" onClick={handleEmailChange}>저장</button>
                      <button className="action-button" onClick={() => { setIsEditingEmail(false); setNewEmail(''); }}>취소</button>
                    </div>
                  </div>) : (
                    <button className="action-button" onClick={() => {
                      if (!isVerified) {
                        Swal.fire({
                          icon: 'warning', title: '본인 인증 필요',
                          text: '본인 인증 후 이메일 변경이 가능합니다.',
                          confirmButtonColor: '#10B981',
                        }); return;
                      } setIsEditingEmail(true);
                    }}> 이메일 변경
                    </button>
                  )}
                </div>
              </div>
              <hr className="divider" />
              {/* 비밀번호 변경 */}
              <div className="setting-card">
                <div className="password-group">
                  <h2 className="group-title">비밀번호 변경</h2>
                  {!isEditingPassword ? (
                    <button className="action-button" onClick={(e) => {
                      e.preventDefault();
                      if (!isVerified) {
                        Swal.fire({
                          icon: "warning", title: "본인 인증 필요",
                          text: "본인 인증 후 비밀번호 변경이 가능합니다.",
                          confirmButtonColor: "#10B981",
                        });
                        return;
                      }
                      setIsEditingPassword(true);}}>
                      비밀번호 변경
                    </button>
                  ) : (
                    <div className="edit-form-container">
                      <input type="password" className="input-field" placeholder="현재 비밀번호"
                        value={currentPw} onChange={(e) => setCurrentPw(e.target.value)} />
                      <input type="password" className="input-field" placeholder="새 비밀번호"
                        value={newPw} onChange={(e) => setNewPw(e.target.value)} />
                      <input type="password" className="input-field" placeholder="새 비밀번호 확인"
                        value={confirmPw} onChange={(e) => setConfirmPw(e.target.value)} />
                      <div className="action-buttons-group" style={{ display: 'flex', gap: '10px', marginTop: "5px" }}>
                        <button className="action-button primary-button"
                          onClick={handlePasswordChange}>저장</button>
                        <button className="action-button"
                          onClick={() => {
                            setIsEditingPassword(false); setCurrentPw('');
                            setNewPw(''); setConfirmPw('');
                          }}>취소
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
              <hr className="divider" />
              {/* 휴대폰 본인 인증 섹션 */}
              <div className='setting-card'>
                <div className="verification-section">
                  <h2 className="section-title">휴대폰 본인 인증</h2>
                  <p className="section-description">
                    회원 정보 변경을 위해 본인 인증을 완료하세요.
                    {/* 인증 상태 표시 */}
                    <span style={{ color: isVerified ? 'green' : 'red', fontWeight: 'bold', marginLeft: '10px' }}>
                      [{isVerified ? '인증 완료' : '인증 필요'}]
                    </span>
                  </p>
                  {/* 휴대폰 인증 버튼 태그 삽입 */}
                  {!isVerified && (
                    <button className="action-button verify-button" onClick={handlePhoneVerification}>
                      휴대폰으로 본인 인증하기
                    </button>
                  )}
                  {isVerified && (
                    <button className="action-button verify-button verified" disabled>
                      인증 완료됨
                    </button>
                  )}
                </div>
              </div>
              <hr className="divider" />
              {/* 테마 선택 */}
              <div className="setting-card">
                <div className="theme-section verification-section">
                  <h2 className="section-title">테마 변경</h2>
                  <p className="section-description">아래에서 상시 적용할 Hangle UI 테마를 선택하세요.</p>
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
              </div>
              <hr className="divider" />
              {/* 계정 삭제 */}
              <div className="setting-card">
                <div className="delete-account-section verification-section">
                  <h2 className="section-title delete-title">Hangle 계정 삭제</h2>
                  <p className="section-description">Hangle 계정을 영구적으로 삭제하세요.</p>
                  <button className="action-button delete-button" onClick={handleDeleteAccount}>
                    계정 삭제
                  </button>
                </div>
              </div>
            </div>
          )}
          {/* 알림 탭 */}
          {activeTab === 'notification' && (
            <div className="notification-settings">
              <div className='setting-card'>
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
            </div>
          )}
        </div>
      </div>
    </section>
  );
};

export default Setting;
