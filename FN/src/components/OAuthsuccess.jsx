import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../api/AuthContext";

const OAuthSuccess = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const username = params.get("username");
    const userid = params.get("userid");
    const role = params.get("role");
    const profileImage = params.get("profileImage");

    if (username && userid && role) {
      // AuthContext의 통합로그인 기능 호출
      login({
        username,
        userid,
        role,
        profileImageUrl: profileImage,
      });

      navigate("/");
    } else {
      navigate("/login");
    }
  }, [location, navigate, login]);

  return <p>로그인 처리 중입니다...</p>;
};

export default OAuthSuccess;
