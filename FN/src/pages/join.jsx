import '../css/login.scss'
import '../css/join.scss'
import { Link, useNavigate } from 'react-router-dom';
import {useState,useEffect} from 'react'
import axios from 'axios'

const Join = () => {
  const [username, setUsername] = useState()
  const [userid, setUserid] = useState()
  const [password, setPassword] = useState()
  const [repassword, setrePassword] = useState()
  const [message, setMessage] = useState(null)
  const [isError, setIsError] = useState(false)
  const navigate = useNavigate();

  useEffect(() => {
    if (repassword && password !== repassword) {
      setIsError(true);
      setMessage("비밀번호가 일치하지 않습니다.");
    } else {
      setIsError(false);
      setMessage(null);
    }
  }, [password, repassword]);

  const handleJoin = async (e) => {
    e.preventDefault();
    setMessage(null);

    if (password !== repassword) {
      setIsError(true);
      setMessage("비밀번호가 일치하지 않습니다.");
      return;
    }

    try {
      const resp = await axios.post(
        'http://localhost:8090/join',
        { username, userid, password, repassword },
        { headers: { 'Content-Type': 'application/json' } }
      );

      setIsError(false);
      setMessage(resp.data.message || '회원가입이 완료되었습니다.');
      
      setTimeout(() => navigate('/login'), 1000);
    } catch (err) {
      const errMsg = err.response?.data?.error || '회원가입 중 오류가 발생했습니다.';
      setIsError(true);
      setMessage(errMsg);
    }
  };

  return (
    <div className="layout-login">
      <div className="login-container">
        <div className="logo" aria-label="로고">
          <Link to="/"><span className="name">Hangle</span></Link>
        </div>
        <form className="register-wrap">
          <div className="input-group">
            <label htmlFor="username">이름</label>
            <input type="text" id="username" name="username" onChange={e=>setUsername(e.target.value)} required />
          </div>
          <div className="input-group">
            <label htmlFor="userid">아이디 (이메일)</label>
            <input type="email" id="userid" name="userid" onChange={e=>setUserid(e.target.value)} required />
          </div>
          <div className="input-group">
            <label htmlFor="password">비밀번호</label>
            <input type="password" id="password" name="password" onChange={e=>setPassword(e.target.value)} required />
          </div>
          <div className="input-group">
            <label htmlFor="repassword">비밀번호 확인</label>
            <input type="password" id="repassword" name="repassword" onChange={e=>setrePassword(e.target.value)} required />
          </div>
          <button onClick={handleJoin}>회원가입</button>
        </form>
        {/* 메시지 표시 영역 */}
        {message && (
          <div
            className={`join-message ${isError ? 'error' : 'success'}`}
            style={{
              color: isError ? '#d9534f' : '#28a745',
              marginTop: '15px',
              fontWeight: '600',
              textAlign: 'center',
            }}
          >
            {message}
          </div>
        )}
        <div className="register-links">이미 계정이 있으신가요?{" "}
          <Link to="/login" className="link">로그인</Link>
        </div>
      </div>
    </div>
  )
}

export default Join