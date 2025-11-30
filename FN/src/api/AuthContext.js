import { createContext, useContext, useState, useEffect } from "react";
import api from "./axiosConfig";

const AuthContext = createContext();
const DEFAULT_AVATAR = "/image/default-avatar.png";

const normalizeProfile = (v) => {
  if (!v) return DEFAULT_AVATAR;
  if (v === "null" || v === "undefined") return DEFAULT_AVATAR;
  return v;
};

export function AuthProvider({ children }) {
  const [isLogin, setIsLogin] = useState(false);
  const [username, setUsername] = useState("");
  const [userid, setUserid] = useState(null);
  const [role, setRole] = useState("");
  const [profileImage, setProfileImage] = useState(DEFAULT_AVATAR);
  const [isLoading, setIsLoading] = useState(true);

  // 공통으로 쓸 "유저 정보 세팅" 함수
  const applyUser = ({ username, userid, role, profileImageUrl, theme }) => {
    const validProfile = normalizeProfile(
      profileImageUrl ? process.env.REACT_APP_API_BASE_URL + profileImageUrl : null
    );
    setUsername(username || "");
    setUserid(userid ?? null);
    setRole(role || "");
    setProfileImage(validProfile);
    setIsLogin(true);

    localStorage.setItem("username", username || "");
    localStorage.setItem("role", role || "");
    localStorage.setItem("profileImage", validProfile);
    if (userid) {
      localStorage.setItem("userid", userid);
    } else {
      localStorage.removeItem("userid");
    }
    if (theme) {
      localStorage.setItem("theme", theme);
    }
  };

  // 공통으로 쓸 "로그아웃/초기화" 함수
  const clearUser = () => {
    setIsLogin(false);
    setUsername("");
    setUserid("");
    setRole("");
    setProfileImage(DEFAULT_AVATAR);

    localStorage.removeItem("username");
    localStorage.removeItem("userid");
    localStorage.removeItem("role");
    localStorage.removeItem("profileImage");
  };

  const login = (data) => {
    applyUser(data);
    setIsLoading(false);
  };

  const logout = () => {
    clearUser();
    setIsLoading(false); // 로그아웃 후에도 로딩 상태는 해제
  };

  // 앱 최초 진입 시 서버에서 로그인 상태 확인
  useEffect(() => {
    const checkAuth = async () => {
      try {
        const res = await api.get("/api/user/me", { withCredentials: true });
        applyUser(res.data);
      } catch (err) {
        // 401/403 등으로 실패하면 "로그인 안 된 상태"로 처리
        console.warn("초기 인증 체크 실패:", err?.response?.status);
        clearUser();
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  // 다른 탭에서 로그인/로그아웃 했을 때 동기화
  useEffect(() => {
    const handleStorageChange = () => {
      const storedUserid = localStorage.getItem("userid");
      if (storedUserid === null) {
        clearUser();
        setIsLoading(false);
        return;
      }

      // 다른 탭에서 로그인한 경우
      const storedUsername = localStorage.getItem("username");
      const storedRole = localStorage.getItem("role");
      const storedProfile = localStorage.getItem("profileImage");

      setUsername(storedUsername || "");
      setUserid(storedUserid || null);
      setRole(storedRole || "");
      setProfileImage(normalizeProfile(storedProfile));
      setIsLogin(true);
      setIsLoading(false);
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        isLogin,
        setIsLogin,
        username,
        setUsername,
        role,
        setRole,
        userid,
        setUserid,
        logout,
        login,
        profileImage,
        setProfileImage,
        isLoading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
