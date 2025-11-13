import { createContext, useContext, useState, useEffect } from "react";
import api from "../api/axiosConfig";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isLogin, setIsLogin] = useState(false);
  const [username, setUsername] = useState('');
  const [userId, setUserId] = useState(null);
  const [role, setRole] = useState('');

  useEffect(() => {
    const storedUsername = localStorage.getItem("username");
    const storedUserId = localStorage.getItem("userid");
    const storedRole = localStorage.getItem("role");

    if (storedUsername) {
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
    setRole("");
    setUserId(null);
    setIsLogin(false);
  };

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await api.get("/validate", { withCredentials: true });
        setIsLogin(true);
      } catch {
        setIsLogin(false);
      }
    };
    checkAuth();
  }, []);

  //OAuthSuccess 처리
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
      setUsername("");
      setUserId(null);
      setRole("");
      setIsLogin(false);
    }
  };

  window.addEventListener("storage", handleStorageChange);
  return () => window.removeEventListener("storage", handleStorageChange);
}, []);

  // username이 비어 있을 때 localStorage를 다시 읽어와서 반영
  useEffect(() => {
    const storedUsername = localStorage.getItem('username');
    if (storedUsername && username !== storedUsername) {
      setUsername(storedUsername);
      setIsLogin(true);
    }
  }, [username]);

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const resp = await api.get("/validate", { withCredentials: true });
        console.log("토큰 유효함:", resp.status);
        setIsLogin(true);
      } catch (err) {
        console.log("토큰 만료 또는 비인증:", err);
        setIsLogin(false);
      }
    };
    checkAuth();
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
