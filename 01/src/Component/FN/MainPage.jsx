

export default function MainPage() {
  const year = new Date().getFullYear()

  return (
    <>
      <div className="layout">
        {/* ===== Sidebar ===== */}
        <aside className="sidebar" aria-label="왼쪽 내비게이션">
          <div className="logo" aria-label="로고">
            <span className="dot" aria-hidden="true"></span>
            <span className="name">hangle</span>
          </div>

          <a className="nav-create" href="#">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              aria-hidden="true"
            >
              <path d="M12 5v14M5 12h14" />
            </svg>
            만들기
          </a>

          <nav className="nav-group">
            <a className="nav-item" href="#">
              {/* trophy */}
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M8 21h8M12 17v4M7 4h10v3a5 5 0 1 0 0-10H7a5 5 0 1 0 0 10V4Z" />
              </svg>
              <span>대회</span>
            </a>
            <a className="nav-item" href="#">
              {/* chat */}
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M21 15a4 4 0 0 1-4 4H8l-5 4V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
              </svg>
              <span>커뮤니티</span>
            </a>
          </nav>
        </aside>

        {/* ===== Topbar ===== */}
        <header className="topbar" aria-label="상단바">
          <div className="search" role="search">
            <span className="icon" aria-hidden="true">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <circle cx="11" cy="11" r="7" />
                <path d="M21 21l-4.3-4.3" />
              </svg>
            </span>
            <input
              type="search"
              placeholder="검색 (데이터셋, 대회, 사용자…)"
              aria-label="검색"
            />
            <span className="kbd">/</span>
          </div>

          <div className="top-actions">
            {/* Dark/Light Toggle */}
            <button
              id="themeToggle"
              className="toggle"
              aria-label="다크 모드 전환"
            >
              <svg
                id="icon-moon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
              <svg
                id="icon-sun"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                style={{ display: 'none' }}
              >
                <circle cx="12" cy="12" r="5" />
                <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
              </svg>
              <span id="modeLabel">다크</span>
            </button>

            {/* 로그인/프로필 (상태에 따라 토글) */}
            <button
              id="loginBtn"
              className="login-btn"
              aria-label="로그인"
              style={{ display: 'none' }}
            >
              로그인
            </button>
            <button
              id="profileBtn"
              className="profile-btn"
              aria-label="프로필로 이동"
              style={{ display: 'none' }}
            >
              <div className="avatar">SD</div>
            </button>
          </div>
        </header>

        {/* ===== Main ===== */}
        <main className="main">
          <section className="welcome">
            <div>
              <h1>welcome to hangle!</h1>
              <p>데이터 시각화 · 커뮤니티 · 대회 정보를 한 곳에서 관리하세요.</p>
            </div>
          </section>

          <h2 className="section-title">요약 지표</h2>
          <section className="stats" aria-label="요약 지표">
            <div className="tile">
              <h4>데이터셋</h4>
              <div className="num">0</div>
              <div className="sub">전체 생성</div>
            </div>
            <div className="tile">
              <h4>Notebook</h4>
              <div className="num">0</div>
              <div className="sub">전체 생성</div>
            </div>
            <div className="tile">
              <h4>대회</h4>
              <div className="num">0</div>
              <div className="sub">참가 수</div>
            </div>
            <div className="tile">
              <h4>연속 접속</h4>
              <div className="num">0</div>
              <div className="sub">일</div>
            </div>
            <div className="tile">
              <h4>누적 접속</h4>
              <div className="num">0</div>
              <div className="sub">일</div>
            </div>
            <div className="tile">
              <h4>여자 친구</h4>
              <div className="num">0</div>
              <div className="sub">명</div>
            </div>
          </section>

          {/* 대회 영역 */}
          <h2 className="section-title">대회</h2>
          <section className="grid" aria-label="시작 카드">
            {/* 카드 영역 그대로 */}
          </section>

          {/* 데이터셋 영역 */}
          <h2 className="section-title">데이터셋</h2>
          <section className="grid" aria-label="데이터셋">
            {/* 카드 영역 그대로 */}
          </section>
        </main>

        {/* ===== Footer ===== */}
        <footer>
          <div className="footer-wrap">
            <div className="footer-grid">
              <div className="footer-col">
                <h4>회사</h4>
                <a href="#">회사 소개</a>
                <a href="#">채용</a>
                <a href="#">보도자료</a>
              </div>
              <div className="footer-col">
                <h4>정책</h4>
                <a href="#">이용약관</a>
                <a href="#">개인정보처리방침</a>
                <a href="#">쿠키 정책</a>
              </div>
              <div className="footer-col">
                <h4>지원</h4>
                <a href="#">도움말 센터</a>
                <a href="#">문의하기</a>
                <a href="#">신고/피드백</a>
              </div>
              <div className="footer-col">
                <h4>리소스</h4>
                <a href="#">개발자 문서</a>
                <a href="#">API</a>
                <a href="#">브랜드 가이드</a>
              </div>
            </div>
            <div className="copyright">
              © {year} hangle. 모든 권리 보유.
            </div>
          </div>
        </footer>
      </div>
    </>
  )
}
