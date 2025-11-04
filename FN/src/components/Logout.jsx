import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const Logout = () => {
  const navigate = useNavigate();

  useEffect(() => {
    
    const performLogout = async () => {
      try {
        // 백엔드 로그아웃 API 호출
        await api.post('/logout', {}, {
          withCredentials: true
        });
        
        // 로그아웃 성공 시 로그인 페이지로 리다이렉트
        navigate('/login');
      } catch (error) {
        console.error('로그아웃 실패:', error);
        // 에러가 발생하더라도 로그인 페이지로 리다이렉트
        navigate('/login');
      }
    };

    performLogout();
  }, [navigate]);

  // 로그아웃 처리 중임을 표시
  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh' 
    }}>
      <p>로그아웃 처리중...</p>
    </div>
  );
};

export default Logout;