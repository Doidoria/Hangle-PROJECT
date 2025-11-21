import { Navigate } from "react-router-dom";
import Swal from "sweetalert2";
import { useAuth } from "../api/AuthContext";

export default function ProtectedRoute({ children, requiredRole }) {
  const { isLogin, role, isLoading } = useAuth();

  // 로딩 중일 때 화면 깜빡임 방지
  if (isLoading) return <div className="loading">Loading...</div>;

  // 로그인이 안 되어 있을 때
  if (!isLogin) {
    Swal.fire({
      icon: "warning",
      title: "로그인이 필요합니다",
      text: "서비스를 이용하기 위해 로그인해주세요.",
      confirmButtonText: "로그인하기",
      confirmButtonColor: "#10B981",
    });
    return <Navigate to="/login" replace />;
  }

  // 역할을 ROLE_ 형태로 정규화
  const normalize = (r) => {
    if (!r) return null;
    const up = r.toUpperCase();
    return up.startsWith("ROLE_") ? up : `ROLE_${up}`;
  };

  const userRole = normalize(role);
  const required = normalize(requiredRole);

  // 역할 계층: USER < MANAGER < ADMIN
  const getLevel = (r) => {
    switch (r) {
      case "ROLE_ADMIN":
        return 3;
      case "ROLE_MANAGER":
        return 2;
      case "ROLE_USER":
        return 1;
      default:
        return 0;
    }
  };

  // requiredRole이 없으면 모두 접근 가능
  let hasRequired = true;
  if (required) {
    hasRequired = getLevel(userRole) >= getLevel(required);
  }

  if (!hasRequired) {
    Swal.fire({
      icon: "error",
      title: "접근 권한이 없습니다",
      text: "이 페이지에 접근할 수 있는 권한이 없습니다.",
      confirmButtonText: "홈으로 이동",
      confirmButtonColor: "#d33",
    });
    return <Navigate to="/" replace />;
  }

  return children;
}
