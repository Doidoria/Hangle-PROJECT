package com.example.demo.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

public class ApiDocs {

    // ============================================
    // USER API
    // ============================================
    @Tag(name = "User", description = "사용자 계정 / 인증 / 설정 API")
    public static class UserDocs {

        @Operation(summary = "회원가입", description = "UserDto를 입력해 신규 계정을 생성합니다.")
        public void join() {}

        @Operation(summary = "로그인", description = "userid + password 로 JWT AccessToken 발급")
        public void login() {}

        @Operation(
                summary = "내 정보 조회",
                description = "JWT 기반으로 로그인한 사용자의 정보를 반환합니다.",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void getUserInfo() {}

        @Operation(
                summary = "소개글 수정",
                description = "사용자 프로필 introduction 필드 수정",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void updateIntroduction() {}

        @Operation(
                summary = "회원 정보 변경",
                description = "username · userid · phone 변경",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void updateUserInfo() {}

        @Operation(
                summary = "비밀번호 변경",
                description = "현재 비밀번호 검증 후 새 비밀번호로 변경",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void changePassword() {}

        @Operation(
                summary = "프로필 이미지 업로드",
                description = "MultipartFile 기반 이미지 업로드 후 URL 반환",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void uploadProfileImage() {}

        @Operation(
                summary = "테마 변경",
                description = "light/dark 테마 저장",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void updateTheme() {}

        @Operation(
                summary = "AccessToken 검증",
                description = "토큰이 유효한지 확인",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void validateToken() {}

        @Operation(
                summary = "회원 탈퇴",
                description = "회원 DB 삭제 + 토큰 폐기 + 강제 로그아웃",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void deleteUser() {}
    }

    // ============================================
    // INQUIRY API
    // ============================================
    @Tag(name = "Inquiry", description = "1:1 문의 API")
    public static class InquiryDocs {

        @Operation(
                summary = "문의 작성",
                description = "로그인 사용자 문의 등록",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void createInquiry() {}

        @Operation(
                summary = "내 문의 목록 조회",
                description = "내가 작성한 모든 문의 조회",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void getMyInquiries() {}

        @Operation(
                summary = "내 문의 삭제",
                description = "본인이 작성한 문의만 삭제 가능",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void deleteMyInquiry() {}

        // =========== ADMIN ===========
        @Operation(
                summary = "[ADMIN] 전체 문의 조회",
                description = "관리자: 모든 문의 조회",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void adminGetAllInquiries() {}

        @Operation(
                summary = "[ADMIN] 문의 답변 등록",
                description = "관리자: 문의에 답변 추가",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void adminAnswerInquiry() {}

        @Operation(
                summary = "[ADMIN] 문의 삭제",
                description = "관리자: 문의 영구 삭제",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void adminDeleteInquiry() {}
    }

    // ============================================
    // COMPETITION API
    // ============================================
    @Tag(name = "Competition", description = "대회 API")
    public static class CompetitionDocs {

        @Operation(summary = "대회 목록 조회", description = "status, keyword 기반 페이징 조회")
        public void getCompetitionList() {}

        @Operation(summary = "대회 상세 조회", description = "ID 기반 상세 조회")
        public void getCompetition() {}

        @Operation(
                summary = "대회 생성",
                description = "새로운 데이터 사이언스 대회 생성",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void createCompetition() {}

        @Operation(
                summary = "대회 수정",
                description = "기존 대회 수정",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void updateCompetition() {}

        @Operation(
                summary = "대회 삭제",
                description = "대회 삭제",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void deleteCompetition() {}
    }

    // ============================================
    // PORTONE API
    // ============================================
    @Tag(name = "PortOne", description = "본인 인증 API")
    public static class PortOneDocs {

        @Operation(
                summary = "본인 인증 결과 조회",
                description = "imp_uid를 기반으로 PortOne에서 인증 결과를 불러와 휴대폰 번호가 일치하는지 확인",
                security = @SecurityRequirement(name = "bearerAuth")
        )
        public void certificationResult() {}
    }
}
