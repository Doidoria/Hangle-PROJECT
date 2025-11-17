import React, { useEffect } from 'react';
import api from '../api/axiosConfig'; // JWT 인증이 포함된 axios 인스턴스

// 환경 변수나 설정 파일에서 가져오는 것을 권장합니다.
const PORTONE_STORE_ID = 'imp78416545'; // PortOne 상점 식별코드 (실제 값으로 변경)
const PORTONE_CHANNEL_KEY = 'channel-key-7f81d708-1905-4424-948e-7ecc770f77fc'; // 본인 인증 채널 키 (실제 값으로 변경)

function PortOneCert() {
    
    useEffect(() => {
        // PortOne SDK 초기화
        // window.IMP가 전역적으로 로드되었는지 확인
        if (window.IMP) {
            window.IMP.init(PORTONE_STORE_ID);
        } else {
            console.error("PortOne SDK (iamport.js)가 로드되지 않았습니다. index.html을 확인하세요.");
        }
    }, []);

    const handleVerification = () => {
        // ESLint 경고를 무시하기 위해 변수에 할당 (ESLint rule: no-undef)
        const IMP = window.IMP; 
        
        if (!IMP) { 
            alert("PortOne SDK 초기화 실패.");
            return;
        }

        // 고유한 주문번호 생성
        const merchant_uid = `cert_${new Date().getTime()}`;

        // PortOne 인증 창 호출
        IMP.certification( // window.IMP 대신 할당된 IMP 사용
            {
                channelKey: PORTONE_CHANNEL_KEY,
                merchant_uid: merchant_uid,
                popup: false, 
            },
            async (resp) => {
                if (resp.success) {
                    const imp_uid = resp.imp_uid;
                    
                    try {
                        // 1. JWT 인증을 포함하여 백엔드 검증 API 호출
                        // (axiosConfig 인터셉터가 Access Token을 자동으로 검증/갱신합니다.)
                        const serverResponse = await api.get(`/portOne/certifications/${imp_uid}`);
                        
                        console.log("백엔드 최종 검증 결과:", serverResponse.data);

                        if (serverResponse.data.isVerified) {
                            alert("✅ 본인 인증 및 휴대폰 번호 확인이 완료되었습니다!");
                            // 인증 성공 시 필요한 추가 로직 (예: 사용자 상태 업데이트, 페이지 리다이렉트)
                        } else {
                            alert(`❌ 최종 본인 인증 실패: ${serverResponse.data.message}`);
                        }

                    } catch (error) {
                        console.error("백엔드 검증 중 오류 발생:", error);
                        // 401 오류는 axiosConfig 인터셉터에서 이미 '/login'으로 리다이렉트 처리했을 것입니다.
                        if (error.response?.status !== 401) { 
                            alert("서버 통신 오류가 발생했습니다.");
                        }
                    }
                    
                } else {
                    alert("인증 창 처리 실패 또는 사용자 취소: " + resp.error_msg);
                }
            }
        );
    };

    return (
        <div style={{ padding: '20px' }}>
            <h3>휴대폰 본인 인증</h3>
            <p>회원 정보에 등록된 휴대폰 번호와 일치하는지 확인합니다.</p>
            <button onClick={handleVerification} 
                    style={{ padding: '10px 20px', cursor: 'pointer' }}>
                휴대폰으로 인증하기
            </button>
        </div>
    );
}

export default PortOneCert;