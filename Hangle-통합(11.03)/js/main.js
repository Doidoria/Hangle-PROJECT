
// ===== 검색 단축키: '/' 포커스 =====
const searchInput = document.querySelector('.search input');
window.addEventListener('keydown', (e) => {
    if (e.key === '/' && document.activeElement !== searchInput) {
        e.preventDefault(); searchInput.focus();
    }
});

// ===== Theme Handling =====
const root = document.documentElement;
const toggleBtn = document.getElementById('themeToggle');
const iconMoon = document.getElementById('icon-moon');
const iconSun = document.getElementById('icon-sun');
const modeLabel = document.getElementById('modeLabel');

const applyTheme = (mode) => {
    const isDark = mode === 'dark';
    root.classList.toggle('theme-dark', isDark);
    iconMoon.style.display = isDark ? 'none' : '';
    iconSun.style.display = isDark ? '' : 'none';
    modeLabel.textContent = isDark ? '라이트' : '다크';
    localStorage.setItem('theme', mode);
};

(function initTheme() {
    const saved = localStorage.getItem('theme');
    if (saved) { applyTheme(saved); }
    else {
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        applyTheme(prefersDark ? 'dark' : 'light');
    }
})();

toggleBtn.addEventListener('click', () => {
    const nowDark = root.classList.contains('theme-dark');
    applyTheme(nowDark ? 'light' : 'dark');
});

window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
    if (!localStorage.getItem('theme')) {
        applyTheme(e.matches ? 'dark' : 'light');
    }
});

// ===== 로그인 상태 토글 (데모) =====
const isLoggedIn = false; // ← 실제 로그인 상태에 맞게 서버에서 주입하세요.
const loginBtn = document.getElementById('loginBtn');
const profileBtn = document.getElementById('profileBtn');
const authCard = document.getElementById('authCard');

function renderAuthUI() {
    if (isLoggedIn) {
        loginBtn.style.display = 'none';
        profileBtn.style.display = 'flex';
        if (authCard) authCard.style.display = 'none'; // 본문 로그인 카드 숨김
    } else {
        loginBtn.style.display = 'inline-flex';
        profileBtn.style.display = 'none';
        if (authCard) authCard.style.display = ''; // 본문 로그인 카드 표시
    }
}
renderAuthUI();

// 데모용 로그인 버튼 (상단바 오른쪽)
loginBtn.addEventListener('click', () => {
    alert('로그인 페이지로 이동합니다.'); // 실제 라우팅으로 교체
    location.href = '/login.html';
});

// 본문 카드의 로그인 버튼(데모)
function mockLogin() {
    alert('로그인 처리 예시. 실제 인증 로직으로 교체하세요.');
}

// 프로필 버튼 클릭 시
profileBtn.addEventListener('click', () => {
    location.href = '/profile.html';
});

// 푸터 연도
document.getElementById('year').textContent = new Date().getFullYear();
