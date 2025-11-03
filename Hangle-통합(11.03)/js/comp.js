// 필요한 DOM 요소 가져오기
const openBtn = document.getElementById('filter-open-btn');
const closeBtn = document.getElementById('filter-close-btn');
const modal = document.getElementById('filter-modal');
const tagButtons = document.querySelectorAll('.tag-button');

// 1. 모달 열기/닫기 로직
function toggleModal() {
    // modal 요소에 'is-open' 클래스를 토글합니다.
    modal.classList.toggle('is-open');

    // 모달이 열리면 스크롤 방지 등의 추가 로직을 여기에 넣을 수 있습니다.
    document.body.style.overflow = modal.classList.contains('is-open') ? 'hidden' : 'auto';
}

// 'Filters' 버튼 클릭 이벤트: 모달 열기
openBtn.addEventListener('click', toggleModal);

// 'Done' 버튼 클릭 이벤트: 모달 닫기
closeBtn.addEventListener('click', toggleModal);

// 모달 밖 클릭 시 닫기
modal.addEventListener('click', function (event) {
    // 모달 배경을 클릭했고 모달이 열린 상태일 때 닫기
    if (event.target === modal) {
        toggleModal();
    }
});


// 2. 필터 태그 버튼 선택 로직 (선택 상태 토글)
tagButtons.forEach(button => {
    button.addEventListener('click', function () {
        // 클릭된 버튼에 'selected' 클래스를 토글합니다.
        this.classList.toggle('selected');

        // **필터링 로직:**
        // 여기서 선택된 필터 값을 수집하여 실제 데이터 필터링 API 호출이나 
        // 화면에 결과를 반영하는 로직을 추가합니다.
    });
});


