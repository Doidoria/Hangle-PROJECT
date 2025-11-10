import { createContext, useContext, useState, useEffect } from "react";
import api from "../api/axiosConfig";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isLogin, setIsLogin] = useState(false);
  const [username, setUsername] = useState("");
  const [userid, setUserid] = useState(null);
  const [role, setRole] = useState("");

  useEffect(() => {
    const storedUsername = localStorage.getItem("username");
    const storedUserid = localStorage.getItem("userid");
    const storedRole = localStorage.getItem("role");

    if (storedUsername && storedUserid) {
      setUsername(storedUsername);
      setUserid(storedUserid);
      setRole(storedRole);
      setIsLogin(true);
    }
  }, []);

  const logout = () => {
    localStorage.removeItem("username");
    localStorage.removeItem("userid");
    localStorage.removeItem("role");
    setUsername("");
    setUserid(null);
    setRole("");
    setIsLogin(false);
  };

  // JWT 토큰 유효성 + 사용자 정보 확인
  useEffect(() => {
    const checkAuth = async () => {
      try {
        const res = await api.get("/validate", { withCredentials: true });
        if (res.status === 200) {
          const userResp = await api.get("/api/user/me", { withCredentials: true });
          const { username, userid, role } = userResp.data;
          setUsername(username);
          setUserid(userid);
          setRole(role);
          setIsLogin(true);
          localStorage.setItem("username", username);
          localStorage.setItem("userid", userid);
          localStorage.setItem("role", role);
        }
      } catch (err) {
        const status = err?.response?.status;
        console.warn("JWT 인증 실패 또는 만료:", status);

        // 401일 경우 자동 재시도 (AccessToken 재발급 후)
        if (status === 401) {
          try {
            const retry = await api.get("/validate", { withCredentials: true });
            if (retry.status === 200) {
              console.log("🔁 AccessToken 자동 재발급 완료");
              const userResp = await api.get("/api/user/me", { withCredentials: true });
              const { username, userid, role } = userResp.data;
              setUsername(username);
              setUserid(userid);
              setRole(role);
              setIsLogin(true);
              localStorage.setItem("username", username);
              localStorage.setItem("userid", userid);
              localStorage.setItem("role", role);
              return;
            }
          } catch (reErr) {
            console.warn("RefreshToken도 만료됨 → 로그아웃");
            logout();
          }
        } else {
          logout();
        }
      }
    };

    checkAuth();
  }, []);

  // 다른 탭 동기화
  useEffect(() => {
    const handleStorageChange = () => {
      const storedUsername = localStorage.getItem("username");
      const storedUserid = localStorage.getItem("userid");
      const storedRole = localStorage.getItem("role");

      if (storedUsername && storedUserid) {
        setUsername(storedUsername);
        setUserid(storedUserid);
        setRole(storedRole);
        setIsLogin(true);
      } else {
        logout();
      }
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
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
