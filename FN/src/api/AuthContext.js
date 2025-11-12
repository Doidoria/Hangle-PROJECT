import { createContext, useContext, useState, useEffect } from "react";
import api from "../api/axiosConfig";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isLogin, setIsLogin] = useState(false);
  const [username, setUsername] = useState("");
  const [userId, setUserId] = useState(null);
  const [role, setRole] = useState("");

  useEffect(() => {
    const storedUsername = localStorage.getItem("username");
    const storedUserId = localStorage.getItem("userid");
    const storedRole = localStorage.getItem("role");

    if (storedUsername && storedUserId) {
      setUsername(storedUsername);
      setUserId(storedUserId);
      setRole(storedRole);
      setIsLogin(true);
    }
  }, []);

  const logout = () => {
    localStorage.removeItem("username");
    localStorage.removeItem("userid");
    localStorage.removeItem("role");
    setUsername("");
    setUserId(null);
    setRole("");
    setIsLogin(false);
  };

  // JWT 토큰 유효성 + 사용자 정보 확인
  useEffect(() => {
    const checkAuthAndFetchUser = async () => {
      try {
        const validateResp = await api.get("/validate", { withCredentials: true });
        if (validateResp.status === 200) {
          setIsLogin(true);

          const userResp = await api.get("/api/user/me", { withCredentials: true });
          if (userResp.status === 200) {
            const { username, userid, role } = userResp.data;
            setUsername(username);
            setUserId(userid);
            setRole(role);

            localStorage.setItem("username", username);
            localStorage.setItem("userid", userid);
            localStorage.setItem("role", role);
          }
        }
      } catch (err) {
        console.warn("JWT 인증 실패 또는 만료:", err?.response?.status);
      }
    };

    checkAuthAndFetchUser();
  }, []);

  // 다른 탭 동기화
  useEffect(() => {
    const handleStorageChange = () => {
      const storedUsername = localStorage.getItem("username");
      const storedUserId = localStorage.getItem("userid");
      const storedRole = localStorage.getItem("role");

      if (storedUsername && storedUserId) {
        setUsername(storedUsername);
        setUserId(storedUserId);
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
        userId,
        setUserId,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
