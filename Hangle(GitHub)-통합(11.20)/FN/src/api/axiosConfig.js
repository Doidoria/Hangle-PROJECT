import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8090",
  withCredentials: true,
});

// ------------------------
// 요청 인터셉터
// ------------------------
api.interceptors.request.use(
  (config) => {
    // 필요하면 여기서 Authorization 헤더 추가 등 처리 가능
    // 현재는 서버가 HttpOnly 쿠키 기반이니까 건드릴 필요 없음
    return config;
  },
  (error) => {
    console.error("[요청 인터셉터 에러]", error);
    return Promise.reject(error);
  }
);

// ------------------------
// 응답 인터셉터
// ------------------------
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const status = error?.response?.status;

    // 토큰 만료 / 인증 실패
    if (status === 401) {
      console.warn("[응답 인터셉터] 401 Unauthorized → 로그인 페이지로 이동");
      // 로컬 스토리지 정리 (AuthContext도 storage 이벤트로 반응)
      localStorage.removeItem("username");
      localStorage.removeItem("userid");
      localStorage.removeItem("role");
      localStorage.removeItem("profileImage");

      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }

      return Promise.reject(error);
    }

    console.error("[응답 인터셉터 에러]", error);
    return Promise.reject(error);
  }
);

// ------------------------
// 302 차단 인터셉터
// ------------------------
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 302) {
      return Promise.reject({
        status: 403,
        message: "권한이 없습니다.",
      });
    }
    return Promise.reject(error);
  }
);

export default api;
