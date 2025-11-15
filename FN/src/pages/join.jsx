import '../css/login.scss'
import '../css/join.scss'
import { Link, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react'
import axios from 'axios'

const Join = () => {
  const [username, setUsername] = useState()
  const [userid, setUserid] = useState()
  const [password, setPassword] = useState()
  const [repassword, setrePassword] = useState()
  const [phone1, setPhone1] = useState('010')
  const [phone2, setPhone2] = useState('')
  const [phone3, setPhone3] = useState('')
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

    const fullPhoneNumber = `${phone1}${phone2}${phone3}`;
    
    if (fullPhoneNumber.length < 10) {
      setIsError(true);
      setMessage("유효한 휴대폰 번호(최소 10자리)를 입력해주세요.");
      return;
    }

    try {
      const resp = await axios.post(
        'http://localhost:8090/join',
        { username, userid, password, repassword, phone: fullPhoneNumber },
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
            <input type="text" id="username" name="username" onChange={e => setUsername(e.target.value)} required />
          </div>
          <div className="input-group">
            <label htmlFor="phone1">휴대폰</label>
            <div style={{ display: 'flex', gap: '5px' }} className='phone-input-group'>
              <select id="phone1" name="phone1"
                      value={phone1} onChange={e => setPhone1(e.target.value)} required>
                  <option value="010">010</option>
                  <option value="011">011</option>
                  <option value="016">016</option>
                  <option value="017">017</option>
                  <option value="018">018</option>
                  <option value="019">019</option>
              </select>
              <input type="text" id="phone2" name="phone2" placeholder="1234" maxLength="4"
                     value={phone2}
                     onChange={e => setPhone2(e.target.value.replace(/[^0-9]/g, ''))} required />
              <input type="text" id="phone3" name="phone3" placeholder="5678" maxLength="4"
                     value={phone3}
                     onChange={e => setPhone3(e.target.value.replace(/[^0-9]/g, ''))} required />
            </div>
          </div>
          <div className="input-group">
            <label htmlFor="userid">아이디 (이메일)</label>
            <input type="email" id="userid" name="userid" onChange={e => setUserid(e.target.value)} required />
          </div>
          <div className="input-group">
            <label htmlFor="password">비밀번호</label>
            <input type="password" id="password" name="password" onChange={e => setPassword(e.target.value)} required />
          </div>
          <div className="input-group">
            <label htmlFor="repassword">비밀번호 확인</label>
            <input type="password" id="repassword" name="repassword" onChange={e => setrePassword(e.target.value)} required />
          </div>
          <button onClick={handleJoin}>회원가입</button>
        </form>
        {message && (
          <div className={`join-message ${isError ? 'error' : 'success'}`}
            style={{ color: isError ? '#d9534f' : '#28a745',
              marginTop: '15px', fontWeight: '600', textAlign: 'center',}}>
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