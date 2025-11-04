import '../css/login.scss'
import { Link, useNavigate } from 'react-router-dom';
import {useState,useEffect} from 'react'
import axios from 'axios' // npm axios 설치
import api from '../api/axiosConfig'; // 새로운 api 인스턴스 임포트

const Login = () => {
    const [username ,setUsername] = useState()
    const [password ,setPassword] = useState()
    const navigate = useNavigate();

  // useEffect에서 API 검증 호출 (최초 처음 실행될때 실행 useEffect)
  useEffect(() => {
    const validateToken = async () => {
      try {
        // 토큰 유효성 검증을 위한 별도 엔드포인트 호출
        const resp = await axios.get("http://localhost:8090/validate", {
          withCredentials: true, // 파라미터 옵션으로 꼭 넣어야지 토큰 쿠키를 전달함.
        });
        console.log("토큰 검증 성공:", resp);
        navigate("/"); // 성공 시 / 경로로 이동
      } catch (error) {
        console.log("토큰 검증 실패:", error);
        // 비정상 응답 시 아무 동작도 하지 않음 (현재 페이지 유지)
      }
    };
    validateToken();
  }, [navigate]); // navigate를 의존성 배열에 추가

    //
    // 로그인 처리 함수
    const handleLogin = async () => {
        try {
            const resp = await api.post(
                "/login",
                { username, password },
                { headers: { "Content-Type": "application/json" } }
            );
            alert("로그인 성공:", resp.data);
            navigate("/"); // 성공 시 / 경로로 이동
        } catch (error) {
            console.error("로그인 실패:", error.response ? error.response.data : error);
            alert("로그인 실패! 다시 시도해주세요."); // 실패 시 메시지 표시
        }
    };

  return (
    <div className="layout-login">
      <div className="login-container">
        <div className="logo" aria-label="로고">
          <Link to="/"><span className="name">Hangle</span></Link>
        </div>
        <form action="/" method="POST" className="login-wrap">
          <div className="input-group">
            <label htmlFor="userid">아이디 (이메일)</label>
            <input type="text" id="userid" name="userid" required />
          </div>
          <div className="input-group">
            <label htmlFor="password">비밀번호</label>
            <input type="password" id="password" name="password" required />
          </div>
          <button type="submit">로그인</button>
        </form>
        <div className="divider">OR</div>
        <Link to="#" className="social-login-button kakao-login">
          <img src="./image/icon_Kakao.png" alt="카카오" /> 카카오 로그인
        </Link>
        <Link to="#" className="social-login-button naver-login">
          <img src="./image/icon_Naver.png" alt="네이버" /> 네이버 로그인
        </Link>
        <Link to="#" className="social-login-button google-login">
          <img src="./image/icon_Google.png" alt="구글" /> 구글 로그인
        </Link>
      </div>
    </div>
  )
}

export default Login;