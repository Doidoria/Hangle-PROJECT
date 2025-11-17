import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../api/AuthContext";

const OAuthSuccess = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { setUsername, setIsLogin } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const username = params.get("username");
    const userid = params.get("userid");

    if (username && userid) {
      localStorage.setItem("username", username);
      localStorage.setItem("userid", userid);
      setUsername(username);
      setIsLogin(true);
      navigate("/");
    } else {
      navigate("/login");
    }
  }, [location, navigate, setUsername, setIsLogin]);

  return <p>로그인 처리 중입니다...</p>;
};

export default OAuthSuccess;
