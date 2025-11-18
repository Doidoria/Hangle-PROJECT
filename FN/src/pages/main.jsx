import '../css/main.scss';
import '../css/media.scss';
import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../api/AuthContext.js';

const Main = () => {
  const { username, isLogin } = useAuth();
  console.log('Main 렌더링:', { username, isLogin });

  useEffect(() => {
    const cards = document.querySelectorAll('.card');
    cards.forEach((card) => {
      card.classList.add('animate');
    });
  }, []);

  return (
    // 메인 전체 래퍼
    <div className="main-page">
      {/* 섹션 1: 웰컴 + 요약 지표 */}
      <section className="section-block section-overview">
        <div className="welcome">
          <div>
            <h1>
              어서 오세요,{' '}
              {isLogin && username ? `${username}님!` : '먼저 로그인을 해주세요!'}
            </h1>
            <p>다양한 데이터 · 커뮤니티 · 대회에 참여하여 본인의 실력을 확인하세요!</p>
          </div>
        </div>

        <div className="stats" aria-label="요약 지표">
          <div className="tile">
            <h4>MY 데이터</h4>
            <div className="num">0</div>
            <div className="sub">전체 생성</div>
          </div>
          <div className="tile">
            <h4>대회</h4>
            <div className="num">0</div>
            <div className="sub">참가 수</div>
          </div>
          <div className="tile">
            <h4>연속 접속</h4>
            <div className="num">0</div>
            <div className="sub">일</div>
          </div>
          <div className="tile">
            <h4>누적 접속</h4>
            <div className="num">0</div>
            <div className="sub">일</div>
          </div>
        </div>
      </section>

      {/* 섹션 2: 추천 대회 */}
      <section className="section-block section-competitions">
        <h2 className="section-title">추천 대회</h2>
        <div className="grid" aria-label="추천 대회 목록">
          <article className="card">
            <span className="badge">추천 대회</span>
            <h3>타이타닉 - 재난으로부터의 머신러닝</h3>
            <p className="muted">
              경쟁은 간단합니다. 머신 러닝을 사용하여 타이타닉호 침몰 사고에서 살아남은 승객을 예측...
            </p>
            <p className="meta">상시 진행 · 입문자 추천</p>
            <Link className="link" to="/competitions">
              자세히 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">추천 대회</span>
            <h3>연애를 할 수 있나요?</h3>
            <p className="muted">
              콜카타에 거주하는 대학생 500명 이상을 대상으로 설문조사를 실시하고, 나이, 몸무게...
            </p>
            <p className="meta">마감 임박 · 설문 데이터</p>
            <Link className="link" to="/competitions">
              자세히 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">추천 대회</span>
            <h3>MABe 챌린지 - 쥐의 사회적 행동 인식</h3>
            <p className="muted">
              이 대회에서는 쥐의 움직임을 기반으로 쥐의 행동을 인식하는 머신 러닝 모델을 개발하여...
            </p>
            <p className="meta">전문가 추천 · 컴퓨터 비전</p>
            <Link className="link" to="/competitions">
              자세히 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">추천 대회</span>
            <h3>교통사고 위험 예측</h3>
            <p className="muted">다양한 도로에서 사고 발생 가능성을 예측합니다.</p>
            <p className="meta">실제 관공서 데이터</p>
            <Link className="link" to="/competitions">
              자세히 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">추천 대회</span>
            <h3>내향적인 사람과 외향적인 사람 예측</h3>
            <p className="muted">
              사회적 행동과 성격 특성을 바탕으로 그 사람이 내향적인지 외향적인지 예측하는 것...
            </p>
            <p className="meta">심리 · 설문 데이터</p>
            <Link className="link" to="/competitions">
              자세히 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">추천 대회</span>
            <h3>칼로리 소모량 예측</h3>
            <p className="muted">운동 중에 얼마나 많은 칼로리가 소모되었는지 예측하는 것이 목표입니다.</p>
            <p className="meta">헬스케어 · 회귀 문제</p>
            <Link className="link" to="/competitions">
              참가하기
            </Link>
          </article>

          <article className="card">
            <span className="badge">추천 대회</span>
            <h3>LOL - 승리하는 방법</h3>
            <p className="muted">ML 모델을 사용하여 누가 이길지 예측</p>
            <p className="meta">게임 로그 · 분류 문제</p>
            <Link className="link" to="/competitions">
              데이터 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">추천 대회</span>
            <h3>톰과 제리 객체 감지</h3>
            <p className="muted">
              이 대회는 톰과 제리를 찾아내기 위해 특별히 큐레이팅된 고유한 데이터 세트를 활용합니다.
            </p>
            <p className="meta">이미지 데이터 · 객체 탐지</p>
            <Link className="link" to="/competitions">
              토론 참여
            </Link>
          </article>
        </div>
      </section>

      {/* 섹션 3: 인기 데이터셋 */}
      <section className="section-block section-datasets">
        <h2 className="section-title">인기 데이터셋</h2>
        <div className="grid" aria-label="데이터셋 목록">
          <article className="card">
            <span className="badge">인기 데이터셋</span>
            <h3>서울시 교통량 통계</h3>
            <p className="muted">노선/시간대별 차량 수 · 혼잡도 시각화</p>
            <p className="meta">공공데이터 · 시계열</p>
            <Link className="link" to="#">
              데이터 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">인기 데이터셋</span>
            <h3>미세먼지 농도 변화</h3>
            <p className="muted">전국 PM2.5/PM10 추세 · 지도 시각화</p>
            <p className="meta">환경 · 시계열 분석</p>
            <Link className="link" to="#">
              데이터 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">인기 데이터셋</span>
            <h3>지역별 비만율 · 식습관</h3>
            <p className="muted">연령/성별 건강지표 · 생활습관 상관분석</p>
            <p className="meta">건강 · 상관분석</p>
            <Link className="link" to="#">
              데이터 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">인기 데이터셋</span>
            <h3>네이버 검색 트렌드(연도별)</h3>
            <p className="muted">관심사 시계열 분석 · 계절성/이벤트 효과</p>
            <p className="meta">트렌드 분석</p>
            <Link className="link" to="#">
              데이터 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">인기 데이터셋</span>
            <h3>대구 아파트 실거래가</h3>
            <p className="muted">평형/연식별 가격 · 지도 기반 시각화</p>
            <p className="meta">부동산 · 회귀</p>
            <Link className="link" to="#">
              데이터 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">인기 데이터셋</span>
            <h3>학교 급식 영양 정보</h3>
            <p className="muted">학교별 칼로리/영양소 · 식단 품질 분석</p>
            <p className="meta">영양 · 통계</p>
            <Link className="link" to="#">
              데이터 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">인기 데이터셋</span>
            <h3>영화 관객 수 · 평점</h3>
            <p className="muted">월별 흥행 추세 · 장르 선호도 분석</p>
            <p className="meta">엔터테인먼트 · 시계열</p>
            <Link className="link" to="#">
              데이터 보기
            </Link>
          </article>

          <article className="card">
            <span className="badge">인기 데이터셋</span>
            <h3>전국 전력 사용량</h3>
            <p className="muted">가정/산업별 소비 패턴 · 계절성 분석</p>
            <p className="meta">에너지 · 시계열</p>
            <Link className="link" to="#">
              데이터 보기
            </Link>
          </article>
        </div>
      </section>
    </div>
  );
};

export default Main;
