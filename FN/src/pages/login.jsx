import '../css/login.scss'
import { Link } from 'react-router-dom';

const Login = () => {
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