import '../css/aside.scss'
import { Link } from 'react-router-dom';

const Aside = () => {
    return (
        <aside className="sidebar" aria-label="왼쪽 내비게이션">
            <Link to="/" className="logo" aria-label="메인 이동">
                <span className="dot" aria-hidden="true" />
                <span className="name">Hangle</span>
            </Link>
            <Link to="/competition" className="nav-create active">
                <img src="/image/+.png" alt="만들기" style={{ width: 14 }} />
                대회 참여
            </Link>
            <nav className="nav-group">
                <Link to="/mydata" className="nav-item" >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                        <path d="M21 15a4 4 0 0 1-4 4H8l-5 4V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
                    </svg>
                    <span>MY데이터</span>
                </Link>
            </nav>
            <nav className="nav-group">
                <Link to="/mydata" className="nav-item" >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                        <path d="M21 15a4 4 0 0 1-4 4H8l-5 4V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
                    </svg>
                    <span>리더보드</span>
                </Link>
            </nav>
            <nav className="nav-group">
                <Link to="/servicecenter" className="nav-item" >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                        <path d="M21 15a4 4 0 0 1-4 4H8l-5 4V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z" />
                    </svg>
                    <span>고객센터</span>
                </Link>
            </nav>
        </aside>
    )
}

export default Aside