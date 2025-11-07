import { createContext, useContext, useState, useEffect } from "react";
import api from "../api/axiosConfig";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isLogin, setIsLogin] = useState(false);
  const [userId, setUserId] = useState(null);

  useEffect(() => {
    const checkAuth = async () => {
      const storedUserId = localStorage.getItem('userid');
      try {
        await api.get("/validate", { withCredentials: true });
        setIsLogin(true);
        setIsLogin(true);
        // 2. 검증 성공 시 userId 상태 업데이트 (로컬 저장소 값이 있다면)
        if (storedUserId) {
          setUserId(storedUserId);
        }
      } catch {
        setIsLogin(false);
        setUserId(null);
      }
    };
    checkAuth();
  }, []);

  return (
    <AuthContext.Provider value={{ isLogin, setIsLogin, userId, setUserId }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
