import '../css/login.scss'
import '../css/join.scss'
import { Link } from 'react-router-dom';
import {useState,useEffect} from 'react'
import axios from 'axios'

const Join = () => {
  const [username, setUsername] = useState()
  const [userid, setUserid] = useState()
  const [password, setPassword] = useState()
  const [repassword, setrePassword] = useState()

  const handleJoin = (e) => {
    axios
      .post(
        'http://localhost:8090/join',
        { "username":userid, "userid": username, "password": password, "repassword": repassword },
        { headers: { 'Content-Type': 'application/json' } }
      )
      .then(resp => {
        console.log(resp)
      })
      .catch(err => { console.log(err) })
  }

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
        <div className="register-links">이미 계정이 있으신가요?{" "}
          <Link to="/login" className="link">로그인</Link>
        </div>
      </div>
    </div>
  )
}

export default Join