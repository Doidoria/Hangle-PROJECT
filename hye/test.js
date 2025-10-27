        document.addEventListener('DOMContentLoaded', () => {
    const sliderWrapper = document.getElementById('menuSliderWrapper');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    
    // 한 번에 스크롤될 거리 (메뉴 항목 너비 정도)
    const scrollDistance = 800; 

    // 현재 스크롤 위치에 따라 버튼 가시성을 업데이트하는 함수
    const updateButtonVisibility = () => {
        // scrollLeft: 현재 스크롤 위치
        // scrollWidth: 스크롤 가능한 전체 콘텐츠 너비
        // clientWidth: 현재 보이는 영역의 너비 (스크롤바 제외)
        const { scrollLeft, scrollWidth, clientWidth } = sliderWrapper;


        //끝에 도달했을때 버튼 제거 및  반대쪽 버튼 생성
        // 왼쪽 끝에 도달했을 때 (스크롤이 0)
        if (scrollLeft <= 5) { // 약간의 여유를 줌 (5px)
            prevBtn.classList.add('hidden');
        } else {
            prevBtn.classList.remove('hidden');
        }

        // 오른쪽 끝에 도달했을 때 (scrollLeft + clientWidth >= scrollWidth)
        if (scrollLeft + clientWidth >= scrollWidth - 5) { // 약간의 여유를 줌 (5px)
            nextBtn.classList.add('hidden');
        } else {
            nextBtn.classList.remove('hidden');
        }
    };

    // '이전' 버튼 클릭 핸들러
    prevBtn.addEventListener('click', () => {
        sliderWrapper.scrollBy({
            left: -scrollDistance, // 왼쪽으로 스크롤
            behavior: 'smooth'      // 부드러운 애니메이션
        });
    });

    // '다음' 버튼 클릭 핸들러
    nextBtn.addEventListener('click', () => {
        sliderWrapper.scrollBy({
            left: scrollDistance, // 오른쪽으로 스크롤
            behavior: 'smooth'     // 부드러운 애니메이션
        });
    });
    
    // 스크롤 이벤트 발생 시 버튼 가시성 업데이트
    sliderWrapper.addEventListener('scroll', updateButtonVisibility);

    // 페이지 로드 시 초기 버튼 가시성 설정
    // 모든 이미지와 콘텐츠가 로드된 후 정확한 scrollWidth를 위해 약간의 지연을 줄 수 있음
    setTimeout(updateButtonVisibility, 100); 
});