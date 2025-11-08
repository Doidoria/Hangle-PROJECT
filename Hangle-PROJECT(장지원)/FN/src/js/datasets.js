document.addEventListener("DOMContentLoaded", () => {
  const likeBtn = document.getElementById("likeBtn");
  const likeCount = document.getElementById("likeCount");

  const storageKey = "liked_dataset";
  let count = Number(localStorage.getItem("like_count")) || 0;
  const alreadyLiked = localStorage.getItem(storageKey) === "true";

  // 초기 표시
  likeCount.textContent = count;

  likeBtn.addEventListener("click", () => {
    if (alreadyLiked) {
      alert("이미 추천하셨습니다.");
      return;
    }

    count++;
    likeCount.textContent = count;
    localStorage.setItem("like_count", count);
    localStorage.setItem(storageKey, "true");
  });
});

document.addEventListener("DOMContentLoaded", () => {
  const tabs = document.querySelectorAll(".tab");
  const contents = document.querySelectorAll(".tab-content");

  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      // 모든 탭 비활성화
      tabs.forEach(t => t.classList.remove("active"));
      // 클릭된 탭 활성화
      tab.classList.add("active");

      // 모든 콘텐츠 숨김
      contents.forEach(c => c.classList.remove("active"));
      // 클릭된 탭과 매칭되는 콘텐츠만 표시
      const target = document.querySelector(`#${tab.dataset.tab}`);
      if (target) target.classList.add("active");
    });
  });
});

const menuToggle = document.getElementById('menuToggle');
const dropdownMenu = document.getElementById('dropdownMenu');

menuToggle.addEventListener('click', (e) => {
  e.stopPropagation(); // 버튼 클릭 시 이벤트 버블링 방지
  dropdownMenu.style.display =
    dropdownMenu.style.display === 'flex' ? 'none' : 'flex';
});

// 다른 곳 클릭 시 메뉴 닫기
document.addEventListener('click', () => {
  dropdownMenu.style.display = 'none';
});

