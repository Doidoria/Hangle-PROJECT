// src/api/axiosConfig.js

import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8090",
  withCredentials: true,
});

// ==========================
// 공용 경로(인증 제외)
// ==========================
const PUBLIC_PATHS = [
  "/login",
  "/join",
  "/validate",
  "/oauth2",
];

// 제출 API는 validate 검사 제외
const isSubmitAPI = (url) =>
  url.includes("/api/competitions") && url.includes("/submit");

// ==========================
// 요청 인터셉터
// ==========================
api.interceptors.request.use(
  async (config) => {
    const token = localStorage.getItem("accessToken");

    // 🔥 Authorization 헤더 자동 설정
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    const url = config.url || "";

    // 🔥 공용 API는 validate 스킵
    if (PUBLIC_PATHS.some((path) => url.startsWith(path))) {
      return config;
    }

    // 🔥 제출 API는 validate 스킵
    if (isSubmitAPI(url)) {
      return config;
    }

    // ================================
    // 🔥 validate 인증 검사
    // ================================
    try {
      await api.get("/validate"); // 반드시 api로 호출해야 인터셉터 작동
      return config;
    } catch (err) {
      console.warn("❌ validate 실패. 로그인 페이지로 이동");
      window.location.href = "/login";
      return Promise.reject("인증 필요");
    }
  },
  (error) => Promise.reject(error)
);

// ==========================
// 응답 인터셉터
// ==========================
api.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    // accessToken 만료 → refreshToken 재발급 요청
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshRes = await api.post("/refresh", null, {
          withCredentials: true,
        });

        const newAccessToken = refreshRes.data?.accessToken;

        if (newAccessToken) {
          localStorage.setItem("accessToken", newAccessToken);

          // 헤더 갱신 후 재요청
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return api(originalRequest);
        }
      } catch (refreshErr) {
        console.error("❌ Refresh Token 재발급 실패 → 로그인 이동");
        window.location.href = "/login";
        return Promise.reject(refreshErr);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
