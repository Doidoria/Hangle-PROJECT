import { createContext, useContext, useState, useEffect } from "react";
import api from "../api/axiosConfig";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [isLogin, setIsLogin] = useState(false);
  const [username, setUsername] = useState('');

  useEffect(() => {
    const storedUsername = localStorage.getItem('username');
      if (storedUsername) {
        setUsername(storedUsername);
        setIsLogin(true);
      }
    }, []);

  const logout = () => {
    localStorage.removeItem('username');
    localStorage.removeItem('userid');
    setUsername('');
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

  return (
    <AuthContext.Provider value={{ isLogin, setIsLogin, username, setUsername, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
