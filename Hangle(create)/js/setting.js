document.addEventListener('DOMContentLoaded', () => {
    const tabItems = document.querySelectorAll('.tab-item');
    const settingscontent = document.querySelector('.settings-content');
    const notificationsettings = document.querySelector('.notification-settings');
    const contentSections = [settingscontent, notificationsettings];

    // 초기 상태 설정: 계정 탭만 표시
    settingscontent.style.display = 'block';
    notificationsettings.style.display = 'none';

    const switchTab = (activeTab, targetContent) => {
        tabItems.forEach(item => item.classList.remove('active'));
        activeTab.classList.add('active');

        contentSections.forEach(section => {
            section.style.display = 'none';
        });
        targetContent.style.display = 'block';
    };

    tabItems.forEach(tab => {
        tab.addEventListener('click', (event) => {
            event.preventDefault();
            const tabText = tab.textContent.trim();
            if (tabText === '계정') {
                switchTab(tab, settingscontent);
            } else if (tabText === '알림') {
                switchTab(tab, notificationsettings);
            }
        });
    });

    const selectContainer = document.querySelector('.custom-select-container');
    const displayButton = selectContainer.querySelector('.select-display-button');
    const optionsList = selectContainer.querySelector('.select-options-list');
    const options = selectContainer.querySelectorAll('.select-option');
    const displayImage = displayButton.querySelector('img');
    const selectedTextSpan = displayButton.querySelector('.selected-text');

    displayButton.addEventListener('click', () => {
        const isExpanded = displayButton.getAttribute('aria-expanded') === 'true'; 
        
        displayButton.setAttribute('aria-expanded', !isExpanded);
        optionsList.style.display = isExpanded ? 'none' : 'block';
    });

    options.forEach(option => {
        option.addEventListener('click', function() {
            const newValue = this.getAttribute('data-value');
            const newTextSpan = this.querySelector('span');
            const newImage = this.querySelector('img');

            const newText = newTextSpan ? newTextSpan.textContent.trim() : '선택 오류';
            const newImageSrc = newImage ? newImage.getAttribute('src') : '';
            const newImageAlt = newImage ? newImage.getAttribute('alt') : '';

            options.forEach(opt => {
                opt.classList.remove('active');
                opt.setAttribute('aria-selected', 'false');
            });
            this.classList.add('active');
            this.setAttribute('aria-selected', 'true');

            selectedTextSpan.textContent = newText;
            if (displayImage) {
                displayImage.setAttribute('src', newImageSrc);
                displayImage.setAttribute('alt', newImageAlt);
            }

            applyTheme(newValue);
            selectContainer.setAttribute('data-current-value',newValue);
            
            displayButton.setAttribute('aria-expanded', 'false');
            optionsList.style.display = 'none';

            console.log(`테마가 ${newValue}로 변경되었습니다. (텍스트: ${newText})`);
        });
    });

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', (event) => {
        if (!selectContainer.contains(event.target)) {
            displayButton.setAttribute('aria-expanded', 'false');
            optionsList.style.display = 'none';
        }
    });
});