// ./js/main.js

document.addEventListener('DOMContentLoaded', () => {
    const tabItems = document.querySelectorAll('.tab-item');
    const accountSettings = document.querySelector('.account-settings');

    const displayBtn=document.querySelector('.')

    // 탭 전환 함수
    const switchTab = (activeTab, targetContent) => {
        // 모든 탭에서 'active' 클래스 제거
        tabItems.forEach(item => item.classList.remove('active'));
        // 클릭된 탭에 'active' 클래스 추가
        activeTab.classList.add('active');

        // 모든 콘텐츠 섹션 숨기기
        contentSections.forEach(section => {
            section.style.display = 'none';
        });

        // 선택된 콘텐츠 섹션 표시
        targetContent.style.display = 'block';
    };

    tabItems.forEach(tab => {
        tab.addEventListener('click', (event) => {
            event.preventDefault(); // 기본 링크 동작 방지
            const tabText = tab.textContent.trim();

            if (tabText === '계정') {
                switchTab(tab, accountSettings);
            } else if (tabText === '알림') {
                switchTab(tab, notificationSettings);
            }
        });
    });
});