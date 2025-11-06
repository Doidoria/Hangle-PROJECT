import '../css/login.scss'
import { Link, useNavigate } from 'react-router-dom';
import {useState,useEffect} from 'react'
import axios from 'axios' // npm axios 설치
import api from '../api/axiosConfig'; // 새로운 api 인스턴스 임포트
import { useAuth } from '../api/AuthContext';

const Login = () => {
    const [userid ,setUserid] = useState()
    const [password ,setPassword] = useState()
    const [message, setMessage] = useState(null)
    const [isError, setIsError] = useState(false)
    const navigate = useNavigate()
    const { setIsLogin } = useAuth()

  // useEffect에서 API 검증 호출 (최초 처음 실행될때 실행 useEffect)
  useEffect(() => {
    const validateToken = async () => {
      try {
        const resp = await axios.get('http://localhost:8090/validate', {
          withCredentials: true,
        })
        console.log('토큰 검증 성공:', resp)
        navigate('/')
      } catch (error) {
        console.log('토큰 검증 실패:', error)
      }
    };
    validateToken()
  }, [navigate])

    const handleLogin = async (e) => {
    e.preventDefault()
    setMessage(null) // 이전 메시지 초기화

    try {
      const resp = await api.post(
        '/login',
        { userid, password },
        {
          headers: { 'Content-Type': 'application/json' },
          withCredentials: true,
        }
      );
      console.log('로그인 성공:', resp.data)
      localStorage.setItem('username', resp.data.username)
      localStorage.setItem('userid', resp.data.userid)
      setIsError(false)
      setMessage(resp.data.message || '로그인에 성공했습니다.')
      setIsLogin(true)
      setTimeout(() => navigate('/'), 1000)
    } catch (error) {
      console.error('로그인 실패:', error)
      const errMsg =
        error.response?.data?.message || '아이디 또는 비밀번호가 올바르지 않습니다.';
      setIsError(true)
      setMessage(errMsg)
    }

    // 로그인 API(카카오, 네이버, 구글)
    const handleSocialLogin = (provider) => {
      window.location.href = `http://localhost:8090/oauth2/authorization/${provider}`;
    };
  };

  return (
    <div className="layout-login">
      <div className="login-container">
        <div className="logo" aria-label="로고">
          <Link to="/"><span className="name">Hangle</span></Link>
        </div>
        <form className="login-wrap">
          <div className="input-group">
            <label htmlFor="userid">아이디 (이메일)</label>
            <input type="text" id="userid" name="userid" onChange={e=>setUserid(e.target.value)} required/>
          </div>
          <div className="input-group">
            <label htmlFor="password">비밀번호</label>
            <input type="password" id="password" name="password" onChange={e=>setPassword(e.target.value)} required/>
          </div>
          <button onClick={handleLogin}>로그인</button>
        </form>
        {/* ✅ 메시지 표시 */}
        {message && (
          <div
            className={`login-message ${isError ? 'error' : 'success'}`}
            style={{
              color: isError ? '#d9534f' : '#28a745',
              marginTop: '15px',
              textAlign: 'center',
              fontWeight: 'bold',
            }}
          >
            {message}
          </div>
        )}
        <div className="login-divider">OR</div>
        <Link onClick={() => handleSocialLogin('kakao')} className="social-login-button kakao-login">
          <img src="./image/icon_Kakao.png" alt="카카오" /> 카카오 로그인
        </Link>
        <Link onClick={() => handleSocialLogin('naver')} className="social-login-button naver-login">
          <img src="./image/icon_Naver.png" alt="네이버" /> 네이버 로그인
        </Link>
        <Link onClick={() => handleSocialLogin('google')} className="social-login-button google-login">
          <img src="./image/icon_Google.png" alt="구글" /> 구글 로그인
        </Link>
        <div className="login-divider">OR</div>
        <Link to="/join" className="social-login-button join-btn">회원가입</Link>
      </div>
    </div>
  )
}

export default Login;