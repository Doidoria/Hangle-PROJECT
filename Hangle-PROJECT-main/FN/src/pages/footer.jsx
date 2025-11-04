import '../css/footer.scss'

const Footer = ()=>{
    return (
        <footer>
          <div className="footer-wrap">
            <div className="footer-grid">
              <div className="footer-col">
                <h4>회사</h4>
                <a href="#">회사 소개</a>
                <a href="#">채용</a>
                <a href="#">보도자료</a>
              </div>
              <div className="footer-col">
                <h4>정책</h4>
                <a href="#">이용약관</a>
                <a href="#">개인정보처리방침</a>
                <a href="#">쿠키 정책</a>
              </div>
              <div className="footer-col">
                <h4>지원</h4>
                <a href="#">도움말 센터</a>
                <a href="#">문의하기</a>
                <a href="#">신고/피드백</a>
              </div>
              <div className="footer-col">
                <h4>리소스</h4>
                <a href="#">개발자 문서</a>
                <a href="#">API</a>
                <a href="#">브랜드 가이드</a>
              </div>
            </div>
            <div className="copyright">
              © <span id="year" /> hangle. 모든 권리 보유.
            </div>
          </div>
        </footer>
    )
}
export default Footer