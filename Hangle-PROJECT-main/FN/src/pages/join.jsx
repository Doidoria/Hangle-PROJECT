import '../css/login.scss'
import '../css/join.scss'
import { Link } from 'react-router-dom';

const Join = () => {
  return (
    <div className="layout-login">
      <div className="login-container">
        <div className="logo" aria-label="로고">
          <Link to="/"><span className="name">Hangle</span></Link>
        </div>
        <form action="/register" method="POST" className="register-wrap">
          <div className="input-group">
            <label htmlFor="username">이름</label>
            <input type="text" id="username" name="username" required />
          </div>
          <div className="input-group">
            <label htmlFor="email">아이디 (이메일)</label>
            <input type="email" id="email" name="email" required />
          </div>
          <div className="input-group">
            <label htmlFor="password">비밀번호</label>
            <input type="password" id="password" name="password" required />
          </div>
          <div className="input-group">
            <label htmlFor="repassword">비밀번호 확인</label>
            <input type="password" id="repassword" name="repassword" required/>
          </div>
          <button type="submit">회원가입</button>
        </form>
        <div className="register-links">이미 계정이 있으신가요?{" "}
          <Link to="/login" className="link">로그인</Link>
        </div>
      </div>
    </div>
  )
}

export default Join