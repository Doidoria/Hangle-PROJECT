import '../css/aside.scss'
import { Link } from 'react-router-dom';

const Aside = () => {
    return (
        <aside className="sidebar" aria-label="왼쪽 내비게이션">
            <Link to="/" className="logo" aria-label="메인 이동">
                <span className="dot" aria-hidden="true" />
                <span className="name">Hangle</span>
            </Link>
            <Link to="/Competitions" className="nav-create active">
                <img src="/image/+.png" alt="만들기" style={{ width: 14 }} />
                대회 참여
            </Link>
            <nav className="nav-group">
                <Link to="/mydata" className="nav-item" >
                    <span class="material-symbols-outlined">dataset</span>
                    <span>마이데이터</span>
                </Link>
            </nav>
            <nav className="nav-group">
                <Link to="/leaderboard" className="nav-item" >
                    <span class="material-symbols-outlined">trophy</span>
                    <span>리더보드</span>
                </Link>
            </nav>
            <nav className="nav-group">
                <Link to="/FaqPage" className="nav-item" >
                    <span class="material-symbols-outlined">support_agent</span>
                    <span>고객센터</span>
                </Link>
            </nav>
        </aside>
    )
}

export default Aside