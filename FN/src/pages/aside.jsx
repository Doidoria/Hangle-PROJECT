import '../css/aside.scss'
import { Link } from 'react-router-dom';
import { useAuth } from '../api/AuthContext';

const Aside = () => {
    const { role, isLoading } = useAuth();
    if (isLoading) return null;

    const userRole = role?.toUpperCase();

    // 일반 USER 메뉴
    const UserMenu = () => (
        <>
            <Link to="/Competitions" className="nav-create active">
                <img src="/image/+.png" alt="참여" style={{ width: 14 }} />
                대회 참여
            </Link>

            <nav className="nav-group">
                <Link to="/mydata" className="nav-item">
                    <span className="material-symbols-outlined">dataset</span>
                    <span>마이데이터</span>
                </Link>
            </nav>

            <nav className="nav-group">
                <Link to="/leaderboard" className="nav-item">
                    <span className="material-symbols-outlined">trophy</span>
                    <span>리더보드</span>
                </Link>
            </nav>

            <nav className="nav-group">
                <Link to="/FaqPage" className="nav-item">
                    <span className="material-symbols-outlined">support_agent</span>
                    <span>고객센터</span>
                </Link>
            </nav>
        </>
    );

    // 매니저 메뉴 — (유저 메뉴 포함 X)
    const ManagerMenu = () => (
        <>
            <Link to="/Competitions" className="nav-create active">
                <img src="/image/+.png" alt="관리" style={{ width: 14 }} />
                대회 참여
            </Link>
            <nav className="nav-group">
                <Link to="/competitions/new" className="nav-item">
                    <span className="material-symbols-outlined">add_circle</span>
                    <span>대회 생성</span>
                </Link>
            </nav>
            <nav className="nav-group">
                <Link to="/competitions" className="nav-item">
                    <span className="material-symbols-outlined">list</span>
                    <span>대회 전체 관리</span>
                </Link>
            </nav>
            <nav className="nav-group">
                <Link to="/admin/inquiries" className="nav-item">
                    <span className="material-symbols-outlined">admin_panel_settings</span>
                    <span>1:1 문의 관리</span>
                </Link>
            </nav>
        </>
    );

    // 관리자 메뉴 — (유저 메뉴 포함 X)
    const AdminMenu = () => (
        <>
            <Link to="/Competitions" className="nav-create active">
                <img src="/image/+.png" alt="관리" style={{ width: 14 }} />
                대회 참여
            </Link>
            <nav className="nav-group">
                <Link to="/competitions/new" className="nav-item">
                    <span className="material-symbols-outlined">add_circle</span>
                    <span>대회 생성</span>
                </Link>
            </nav>
            <nav className="nav-group">
                <Link to="/competitions" className="nav-item">
                    <span className="material-symbols-outlined">list</span>
                    <span>대회 전체 관리</span>
                </Link>
            </nav>
            <nav className="nav-group">
                <Link to="/admin/inquiries" className="nav-item">
                    <span className="material-symbols-outlined">admin_panel_settings</span>
                    <span>1:1 문의 관리</span>
                </Link>
            </nav>
        </>
    );

    return (
        <aside className="sidebar" aria-label="왼쪽 내비게이션">
            <Link to="/" className="logo" aria-label="메인 이동">
                <span className="dot" aria-hidden="true" />
                <span className="name">Hangle</span>
            </Link>

            {/* 권한별로 완전히 분리 출력 */}
            {{
                'ROLE_ADMIN': <AdminMenu />,
                'ROLE_MANAGER': <ManagerMenu />,
                'ROLE_USER': <UserMenu />
            }[userRole] || <UserMenu />}
        </aside>
    );
};

export default Aside;
