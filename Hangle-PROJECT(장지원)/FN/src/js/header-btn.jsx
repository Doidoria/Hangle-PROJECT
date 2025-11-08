import { useEffect, useState, useRef } from "react";
import { Link } from 'react-router-dom';

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
      <input ref={inputRef} type="search" placeholder="검색 (데이터셋, 대회, 사용자…)" aria-label="검색"/>
      <span className="kbd">/</span>
    </div>
  );
}

// 다크, 라이트 버튼
function ThemeToggle() {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem("theme") ||
      (window.matchMedia("(prefers-color-scheme: dark)").matches
        ? "dark"
        : "light");
  });

  useEffect(() => {
    document.documentElement.classList.toggle("theme-dark", theme === "dark");
    localStorage.setItem("theme", theme);
  }, [theme]);

  useEffect(() => {
    const mq = window.matchMedia("(prefers-color-scheme: dark)");
    const listener = (e) => {
      if (!localStorage.getItem("theme")) {
        setTheme(e.matches ? "dark" : "light");
      }
    };
    mq.addEventListener("change", listener);
    return () => mq.removeEventListener("change", listener);
  }, []);

  return (
    <button onClick={() => setTheme(theme === "dark" ? "light" : "dark")} className="toggle">
      {theme === "dark" ? "🌙 다크" : "☀️ 라이트"}
    </button>
  );
}

// 로그인 버튼
function Loginbtn(){
  return(
    <Link to='/login' id="loginBtn" className="login-btn" aria-label="로그인">로그인</Link>
  )
}

//프로필 버튼
function Profilebtn(){
  return(
    <Link to='/profile' id="profileBtn" className="profile-btn" aria-label="프로필">
      <div className="avatar">SD</div>
    </Link>
  )
}

export {SearchBox, ThemeToggle, Loginbtn, Profilebtn};
