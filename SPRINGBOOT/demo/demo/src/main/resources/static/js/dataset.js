document.addEventListener("DOMContentLoaded", () => {
  const loadBtn = document.getElementById("loadBtn");
  const sortBtn = document.getElementById("sortBtn");
  let sortMode = "date";

  // ✅ 로그인 사용자 (나중에 서버 세션 or JWT 기반으로 교체)
  const currentUser = {
    username: "관리자",
    role: "ADMIN"
  };

  // ======================
  // 1️⃣ 데이터 불러오기
  // ======================
  async function loadDatasets() {
    const url =
      sortMode === "likes"
        ? "http://localhost:8088/api/datasets/sort/likes"
        : "http://localhost:8088/api/datasets";

    try {
      const res = await fetch(url);
      const data = await res.json();
      const container = document.getElementById("datasetContainer");
      container.innerHTML = "";

      if (data.length === 0) {
        container.innerHTML = "<p>등록된 데이터가 없습니다.</p>";
        sortBtn.disabled = true;
        return;
      }

      // loadDatasets 내부에 카드 출력부 수정
      data.forEach(item => {
        const canEdit =
          item.author === currentUser.username ||
          currentUser.role === "ADMIN";

        const liked = localStorage.getItem(`liked_${item.id}`) === "true";

        const div = document.createElement("div");
        div.className = "dataset-card";
        div.innerHTML = `
          <h3>${item.title}</h3>
          <p>${item.description}</p>
          <p><strong>작성자:</strong> ${item.author}</p>
          <p><strong>등록일:</strong> ${new Date(item.createdAt).toLocaleString()}</p>
          <p><strong>좋아요:</strong> <span id="like-count-${item.id}">${item.likes}</span></p>
          <button class="like-btn ${liked ? "active" : ""}" data-id="${item.id}">
            ${liked ? "❤️ 좋아요" : "👍 좋아요"}
          </button>
          ${canEdit
            ? `<button class="edit-btn" data-id="${item.id}">✏️ 수정</button>
               <button class="delete-btn" data-id="${item.id}">🗑️ 삭제</button>`
            : ""}
        `;
        container.appendChild(div);
      });

      sortBtn.disabled = false;
    } catch (err) {
      console.error("❌ 데이터 로드 실패:", err);
      alert("데이터 불러오기 오류 발생");
      sortBtn.disabled = true;
    }
  }

  // ======================
  // 2️⃣ 불러오기 버튼
  // ======================
  if (loadBtn) {
    loadBtn.addEventListener("click", async () => {
      loadBtn.disabled = true;
      loadBtn.textContent = "불러오는 중...";
      await loadDatasets();
      loadBtn.disabled = false;
      loadBtn.textContent = "데이터 불러오기";
    });
  }

  // ======================
  // 3️⃣ 정렬 버튼
  // ======================
  if (sortBtn) {
    sortBtn.addEventListener("click", () => {
      if (sortBtn.disabled) return;
      sortMode = sortMode === "date" ? "likes" : "date";
      sortBtn.textContent = sortMode === "date" ? "📅 등록일순" : "❤️ 좋아요순";
      loadDatasets();
    });
  }

  // ======================
  // 4️⃣ 좋아요 버튼 (토글 방식)
  // ======================
  document.addEventListener("click", async e => {
    if (e.target.classList.contains("like-btn")) {
      const id = e.target.dataset.id;
      const liked = localStorage.getItem(`liked_${id}`) === "true";

      try {
        const res = await fetch(`http://localhost:8088/api/datasets/${id}/like`, {
          method: liked ? "DELETE" : "PUT"
        });
        const updated = await res.json();

        const countEl = document.getElementById(`like-count-${id}`);
        if (countEl) countEl.textContent = updated.likes;

        if (liked) {
          localStorage.removeItem(`liked_${id}`);
          e.target.classList.remove("active");
          e.target.textContent = "👍 좋아요";
        } else {
          localStorage.setItem(`liked_${id}`, "true");
          e.target.classList.add("active");
          e.target.textContent = "❤️ 좋아요";
        }
      } catch (err) {
        console.error("좋아요 처리 실패:", err);
        alert("좋아요 처리 중 오류 발생");
      }
    }
  });

  // ======================
  // 5️⃣ 등록 (입력 검증)
  // ======================
  const btn = document.getElementById("submitDataset");
  if (btn) {
    btn.addEventListener("click", () => {
      const title = document.getElementById("title").value.trim();
      const description = document.getElementById("description").value.trim();
      const author = document.getElementById("author").value.trim();

      if (!title || !description || !author) {
        alert("⚠️ 제목, 설명, 작성자를 모두 입력해주세요.");
        return;
      }

      const newDataset = { title, description, author, likes: 0 };

      fetch("http://localhost:8088/api/datasets", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newDataset)
      })
        .then(res => res.json())
        .then(data => {
          console.log("✅ 등록 성공:", data);
          alert("데이터셋이 등록되었습니다.");
          location.reload();
        })
        .catch(err => console.error("❌ 등록 실패:", err));
    });
  }

  // ======================
  // 6️⃣ 수정 / 삭제
  // ======================
  document.addEventListener("click", e => {
    const id = e.target.dataset.id;

    if (e.target.classList.contains("edit-btn")) {
      const title = prompt("새 제목 입력:");
      const description = prompt("새 설명 입력:");
      const author = prompt("새 작성자 입력:");
      if (!title || !description || !author) return;

      fetch(`http://localhost:8088/api/datasets/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, description, author })
      })
        .then(res => res.json())
        .then(() => {
          alert("수정되었습니다.");
          loadDatasets();
        })
        .catch(err => console.error("수정 실패:", err));
    }

    if (e.target.classList.contains("delete-btn")) {
      if (!confirm("정말 삭제하시겠습니까?")) return;

      fetch(`http://localhost:8088/api/datasets/${id}`, {
        method: "DELETE"
      })
        .then(() => {
          alert("삭제되었습니다.");
          loadDatasets();
        })
        .catch(err => console.error("삭제 실패:", err));
    }
  });
});
