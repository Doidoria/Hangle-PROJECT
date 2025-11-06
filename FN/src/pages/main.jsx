import '../css/main.scss'
import '../css/media.scss'
import Layout from './Layout.jsx'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom';
import { useAuth } from "../api/AuthContext.js";

const Main = () => {
    const { username, isLogin } = useAuth();

    return (
        <Layout>
            <section>
                <div className="welcome">
                    <div>
                        <h1>어서 오세요, {isLogin && username ? `${username}님!` : '먼저 로그인을 해주세요!'}</h1>
                        <p>다양한 데이터 · 커뮤니티 · 대회에 참여하여 본인의 실력을 확인하세요!</p>
                    </div>
                </div>
                <div className="stats" aria-label="요약 지표">
                    <div className="tile">
                        <h4>데이터셋</h4>
                        <div className="num">0</div>
                        <div className="sub">전체 생성</div>
                    </div>
                    <div className="tile">
                        <h4>노트북</h4>
                        <div className="num">3</div>
                        <div className="sub">전체 생성</div>
                    </div>
                    <div className="tile">
                        <h4>대회</h4>
                        <div className="num">0</div>
                        <div className="sub">참가 수</div>
                    </div>
                    <div className="tile">
                        <h4>토론</h4>
                        <div className="num">0</div>
                        <div className="sub">작성 수</div>
                    </div>
                    <div className="tile">
                        <h4>강좌</h4>
                        <div className="num">0</div>
                        <div className="sub">완료 수</div>
                    </div>
                    <div className="tile">
                        <h4>연속 접속</h4>
                        <div className="num">2</div>
                        <div className="sub">일</div>
                    </div>
                </div>
                {/* 시작 카드 */}
                <div className="grid" aria-label="추천 섹션">
                    <article className="card">
                        <span className="badge">가이드</span>
                        <h3>대회 참여 방법</h3>
                        <p className="muted">규칙, 평가 지표, 제출 형식을 빠르게 이해해보세요.</p>
                        <a className="link" href="#">자세히 보기</a>
                    </article>
                    <article className="card">
                        <h3>샘플 차트</h3>
                        <div className="chart" aria-hidden="true" />
                        <p className="muted" style={{ marginTop: 8 }}>차트/카드 대비를 확인하는 더미 요소입니다.</p>
                    </article>
                    {/* 스크롤을 위한 추가 카드들 */}
                    <article className="card">
                        <span className="badge">추천 대회</span>
                        <h3>이미지 분류 챌린지</h3>
                        <p className="muted">CNN · Transfer Learning · 제출 마감 D-12</p>
                        <a className="link" href="#">참가하기</a>
                    </article>
                    <article className="card">
                        <span className="badge">인기 데이터셋</span>
                        <h3>서울시 교통량 통계</h3>
                        <p className="muted">월별/노선별 집계 · 시각화 예제 포함</p>
                        <a className="link" href="#">데이터 보기</a>
                    </article>
                    <article className="card">
                        <span className="badge">커뮤니티</span>
                        <h3>초보자를 위한 EDA 팁</h3>
                        <p className="muted">결측치 처리, 이상치 탐지, 시각화 모범 예시</p>
                        <a className="link" href="#">토론 참여</a>
                    </article>
                    <article className="card">
                        <span className="badge">가이드</span>
                        <h3>대회 제출 체크리스트</h3>
                        <ul className="muted" style={{ margin: "6px 0 0 18px" }}>
                            <li>파일 포맷/컬럼명 확인</li>
                            <li>스코어 산식 일치 여부</li>
                            <li>베이스라인과 비교</li>
                        </ul>
                    </article>
                    <article className="card">
                        <span className="badge">코스</span>
                        <h3>파이썬으로 시작하는 머신러닝</h3>
                        <p className="muted">기초 문법 → Pandas → 모델링까지</p>
                        <a className="link" href="#">수강하기</a>
                    </article>
                    <article className="card">
                        <h3>리더보드 살펴보기</h3>
                        <p className="muted">상위권 노트북의 전처리/모델 설정을 비교해 보세요.</p>
                        <a className="link" href="#">리더보드 이동</a>
                    </article>
                </div>
            </section>
        </Layout>
    )
}

export default Main