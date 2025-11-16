import axios from "axios";

// validate 중복 호출 방지 플래그
let isValidating = false;

const api = axios.create({
  baseURL: "http://localhost:8090",
  withCredentials: true,
});

/* ================================
   인증 제외 경로
================================ */
const PUBLIC_PATHS = [
  "/login",
  "/join",
  "/oauth2",
];

const isSubmitAPI = (url) =>
  url.includes("/api/competitions") && url.includes("/submit");

/* ================================
   요청 인터셉터
================================ */
api.interceptors.request.use(
  async (config) => {
    const url = config.url || "";
    const token = localStorage.getItem("accessToken");

    /* 1) Authorization 헤더는 최상단에서 무조건 먼저 넣는다 */
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    /* 2) 제출 API는 validate 생략 (중요) */
    if (isSubmitAPI(url)) {
      return config;
    }

    /* 3) 로그인/회원가입도 validate 생략 */
    const isPublic = PUBLIC_PATHS.some((path) => url.startsWith(path));
    if (isPublic) {
      return config;
    }

    /* 4) validate 중복 호출 방지 */
    if (!isValidating) {
      isValidating = true;

      try {
        // validate 요청 -> 반드시 Authorization 헤더 포함됨
        await api.get("/validate");
      } catch (e) {
        isValidating = false;
        window.location.href = "/login";
        return Promise.reject(e);
      }

      isValidating = false;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

/* ================================
   응답 인터셉터 (401 처리)
================================ */
api.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    // refresh 요청이 401이면 재발급 불가 → 로그인 이동
    if (originalRequest.url?.includes("/refresh")) {
      window.location.href = "/login";
      return Promise.reject(error);
    }

    // AccessToken 만료 → RefreshToken 사용해 재발급
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const res = await api.post("/refresh", null, { withCredentials: true });

        const newToken = res.data?.accessToken;

        if (newToken) {
          localStorage.setItem("accessToken", newToken);
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return api(originalRequest);
        }
      } catch (refreshErr) {
        window.location.href = "/login";
        return Promise.reject(refreshErr);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
