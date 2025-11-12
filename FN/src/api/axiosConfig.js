import axios from 'axios';

// axios 인스턴스 생성 (모듈화)
const api = axios.create({
  baseURL: 'http://localhost:8090',
  withCredentials: true, // HTTP-Only 쿠키 포함
});

//------------------------
// 요청 인터셉터 설정
//------------------------
api.interceptors.request.use(
  async (config) => {
    // 로그인 페이지나 회원가입 페이지 등 인증이 필요없는 경로는 제외
    const publicPaths = ['/login', '/join','/validate'];
    if (publicPaths.some(path => config.url.includes(path))) {
      return config;
    }

    try {
      // 토큰 유효성 검증을 위한 별도 엔드포인트 호출
      await axios.get('http://localhost:8090/validate', {
        withCredentials: true
      });
      console.log("[정상-요청 인터셉터] 인증된 상태입니다");
      return config;

    } catch (error) {
      console.log("[오류-요청 인터셉터] ",error);
      window.location.href = '/login';
      return Promise.reject('인증이 필요합니다.');
    }
  },
  (error) => {
    console.log("[오류-요청 인터셉터] ",error);
    window.location.href = '/login';
    return Promise.reject(error);
  }
);

//------------------------
// 응답 인터셉터 설정
//------------------------
api.interceptors.response.use(
  (response) => {
    console.log("[정상-응답 인터셉터] ",response);
    if (response.data?.auth === false) {
      window.location.href = '/login';
      return Promise.reject('세션이 만료되었습니다.');
    }
    return response;
  },
  (error) => {
  
    console.log("[오류-응답 인터셉터] ",error);
    if (error.response?.data?.expired === true) {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api; 