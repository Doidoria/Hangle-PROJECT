import { useEffect, useState, useRef } from "react";
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from "../api/AuthContext.js";
import { useTheme } from '../api/ThemeContext';
import api from '../api/axiosConfig';

function SearchBox() {
  const inputRef = useRef(null);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "/" && document.activeElement !== inputRef.current) {
        e.preventDefault();
        inputRef.current?.focus();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  return (
    <div className="search" role="search">
      <span className="icon" aria-hidden="true">
        <svg width={20} height={20} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
          <circle cx={11} cy={11} r={7} />
          <path d="M21 21l-4.3-4.3" />
        </svg>
      </span>
      <input ref={inputRef} type="search" placeholder="검색 (데이터셋, 대회, 사용자…)" aria-label="검색" />
      <span className="kbd">/</span>
    </div>
  );
}

// 다크, 라이트 모드
function ThemeToggle() {
  const { theme, setTheme } = useTheme();

  const handleToggle = async () => {
    const newTheme = theme === "dark" ? "light" : "dark";
    setTheme(newTheme);

    try {
      await api.put("/api/user/theme", { theme: newTheme });
      localStorage.setItem("theme", newTheme);
    } catch (err) {
      console.error("테마 저장 실패", err);
    }
  };

  return (
    <button onClick={handleToggle} className="toggle" aria-label="테마 전환">
      {theme === "dark" ? (
        <img src="./image/icon_moon(white).png" alt="다크 모드" className="theme-icon" />
      ) : (
        <img src="./image/icon_sun(black).png" alt="라이트 모드" className="theme-icon" />
      )}
    </button>
  );
}

// 로그인/로그아웃 버튼
function HeaderButtons() {
  const { isLogin, setIsLogin, username, setUsername } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  // 로그인 / 회원가입 페이지에서는 Header 자체 숨김
  if (location.pathname === '/login' || location.pathname === '/join') {
    return null;
  }

  const handleLogout = async () => {

    try {
      const resp = await api.post("/logout", {}, { withCredentials: true });
      console.log("로그아웃 응답:", resp.data);
      localStorage.removeItem('username');
      localStorage.removeItem('userid');
      setUsername(''); // username 상태 초기화
      setIsLogin(false);
      navigate("/login", { replace: true });
    } catch (error) {
      console.error("로그아웃 실패:", error);
    } finally { // 항상 로컬 스토리지와 상태 초기화
      localStorage.removeItem('username');
      localStorage.removeItem('userid');
      setUsername('');
      setIsLogin(false);
      // navigate("/login", { replace: true });
    }
  }

  return (
    <>
      {isLogin ? (
        <Link onClick={handleLogout} id="logoutBtn" className="logout-btn">로그아웃</Link>
      ) : (
        <Link to='/login' id="loginBtn" className="login-btn">로그인</Link>
      )}
    </>
  );
};

//프로필 버튼
function Profilebtn() {
  const { profileImage } = useAuth();
  const DEFAULT_AVATAR = "/image/default-avatar.png";
  const safeSrc = !profileImage || profileImage === "null" || profileImage === "undefined"
    ? DEFAULT_AVATAR : profileImage;

  return (
    <Link to="/myprofile" id="profileBtn" className="profile-btn" aria-label="프로필">
      <img src={safeSrc} onError={(e) => (e.currentTarget.src = DEFAULT_AVATAR)}
        alt="Profile" className="avatar-img" />
    </Link>
  );
}

export { SearchBox, ThemeToggle, HeaderButtons, Profilebtn };
