import Swal from 'sweetalert2';
import { Navigate } from "react-router-dom";
import { useAuth } from "../api/AuthContext";

function ProtectedRoute({ children, requiredRole }) {
  const { isLogin, role } = useAuth();

  const DEV_MODE = true; // 개발 중엔 true, 배포할 때 false (지워도 됨)

  if (!DEV_MODE && !isLogin) {
    Swal.fire({
      icon: 'warning',
      title: '로그인이 필요합니다',
      text: '서비스를 이용하려면 로그인해주세요.',
      confirmButtonText: '로그인으로 이동',
      confirmButtonColor: '#10B981'
    });
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && role !== requiredRole) {
    Swal.fire({
      icon: 'error',
      title: '접근 권한이 없습니다',
      text: '이 페이지에 접근할 수 있는 권한이 없습니다.',
      confirmButtonText: '홈으로 돌아가기',
      confirmButtonColor: '#d33'
    });
    return <Navigate to="/" replace />;
  }

  return children;
}

export default ProtectedRoute;
