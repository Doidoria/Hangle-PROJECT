import Swal from "sweetalert2";
import { Navigate } from "react-router-dom";
import { useAuth } from "../api/AuthContext";

function ProtectedRoute({ children, requiredRole }) {
  const { isLogin, role, isLoading } = useAuth();

  // 아직 AuthContext가 서버에서 로그인 상태 확인 중일 때
  if (isLoading) {
    return <div>Loading...</div>;
  }

  // 역할 문자열 통일 (ROLE_ADMIN / ADMIN 등)
  const userRole = role ? role.toUpperCase() : null;
  const required = requiredRole ? requiredRole.toUpperCase() : null;

  let hasRequiredRole = false;
  if (required) {
    if (userRole === required) {
      hasRequiredRole = true;
    } else if (userRole && userRole.startsWith("ROLE_")) {
      // 사용자: ROLE_ADMIN, 필요: ADMIN
      if (userRole.replace("ROLE_", "") === required) hasRequiredRole = true;
    } else if (required.startsWith("ROLE_")) {
      // 사용자: ADMIN, 필요: ROLE_ADMIN
      if (required.replace("ROLE_", "") === userRole) hasRequiredRole = true;
    }
  } else {
    // requiredRole 안 넘기면 "로그인만 되어 있으면 통과"
    hasRequiredRole = true;
  }

  if (!isLogin) {
    Swal.fire({
      icon: "warning",
      title: "로그인이 필요합니다",
      text: "서비스를 이용하려면 로그인해주세요.",
      confirmButtonText: "로그인으로 이동",
      confirmButtonColor: "#10B981",
    });
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRequiredRole) {
    Swal.fire({
      icon: "error",
      title: "접근 권한이 없습니다",
      text: "이 페이지에 접근할 수 있는 권한이 없습니다.",
      confirmButtonText: "홈으로 돌아가기",
      confirmButtonColor: "#d33",
    });
    return <Navigate to="/" replace />;
  }

  return children;
}

export default ProtectedRoute;
