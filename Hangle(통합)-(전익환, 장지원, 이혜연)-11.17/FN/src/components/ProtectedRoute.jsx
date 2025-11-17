import Swal from 'sweetalert2';
import { Navigate } from "react-router-dom";
import { useAuth } from "../api/AuthContext";

function ProtectedRoute({ children, requiredRole }) {
  const { isLogin, role, isLoading } = useAuth();
  
  const DEV_MODE = true; // 개발 중엔 true, 배포할 때 false (지워도 됨)

  // 1. 로딩 중일 때 (role 상태가 결정되지 않았을 때)는 대기
  if (isLoading) {
    return <div>Loading...</div>;
  }

  // 역할 불일치 문제를 해결하기 위해, role 값을 표준화합니다. (기존 로직 유지)
  const userRole = role ? role.toUpperCase() : null;
  const required = requiredRole ? requiredRole.toUpperCase() : null;

  // 역할 포맷이 'ROLE_ADMIN' vs 'ADMIN'처럼 달라도 허용하도록 유연하게 검증합니다.
  let hasRequiredRole = false;
  if (required) {
    if (userRole === required) {
      hasRequiredRole = true;
    }
    // 'ROLE_' 접두사를 제거한 후 일치하는 경우 (예: 'ADMIN' === 'ADMIN' 또는 'ROLE_ADMIN' === 'ADMIN')
    else if (userRole && userRole.includes('_')) {
      // 사용자 역할이 'ROLE_ADMIN'인데, 필수 역할이 'ADMIN'인 경우
      if (userRole.replace('ROLE_', '') === required) {
        hasRequiredRole = true;
      }
    } else if (required.includes('_')) {
      // 사용자 역할이 'ADMIN'인데, 필수 역할이 'ROLE_ADMIN'인 경우
      if (required.replace('ROLE_', '') === userRole) {
        hasRequiredRole = true;
      }
    }
  } else {
    // requiredRole이 설정되지 않았으면, 로그인만 되어 있으면 접근 허용
    hasRequiredRole = true;
  }

  if (!isLogin) {
    Swal.fire({
      icon: 'warning',
      title: '로그인이 필요합니다',
      text: '서비스를 이용하려면 로그인해주세요.',
      confirmButtonText: '로그인으로 이동',
      confirmButtonColor: '#10B981'
    });
    return <Navigate to="/login" replace />;
  }

  // requiredRole이 설정되었는데, 접근 권한이 없는 경우
  if (requiredRole && !hasRequiredRole) {
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

