import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from '../api/axiosConfig'; // 새로운 api 인스턴스 임포트 (axiosConfig)

const User = () => {
  const [userInfo, setUserInfo] = useState(null);
  const [error, setError] = useState(null);
  const [renewal, setRenewal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        // axios 대신 커스텀 api 인스턴스 사용 (axiosConfig)
        const resp = await api.get("/user"); // 유저 정보 요청
        console.log(resp.data);
        setUserInfo(resp.data);

        if(resp.data.renewal && resp.data.renewal === true) {
          setRenewal(true);
        }

        setError(null);
      } catch (err) {
        console.error("User info fetch error:", err);
        setError("사용자 정보를 가져오는 데 실패했습니다.");
      }
    };
    fetchUserInfo();
  }, [renewal]);

  return (
    <div>
      <h1>USER PAGE</h1>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {userInfo && (
        <div>
          <h2>사용자 정보</h2>
          <p>Username: {userInfo.username}</p>
          <p>Authenticated: {userInfo.role}</p>
        </div>
      )}
    </div>
  );
};

export default User;
