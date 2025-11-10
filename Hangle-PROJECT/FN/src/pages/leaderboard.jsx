import Layout from './Layout.jsx'
import { Link } from 'react-router-dom';
import '../css/leaderboard.scss'

const Leaderboard = () => {
    return (
        <Layout>

            <main className="main">
                <section className="section-wrap">
                    <div>
                        <h1>이미지 분류 챌린지 (리더보드) 🏆</h1>
                        <p>상위권 참가자의 점수를 확인하세요.</p>
                    </div>
                    <div className="card" style={{ overflowX: "auto" }}>
                        <table
                            className="leaderboard"
                            style={{ width: "100%", borderCollapse: "collapse" }}
                        >
                            <thead>
                                <tr>
                                    <th>순위</th>
                                    <th>닉네임</th>
                                    <th>점수</th>
                                    <th>제출 횟수</th>
                                    <th>최근 제출일</th>
                                </tr>
                            </thead>
                            <tbody className="leaderboardBody" />
                        </table>
                    </div>
                </section>
            </main>

        </Layout>


    )
}

export default Leaderboard



//실제 응답 예시
//[
//        {
//        "rank": 1,
//        "userid": "hyun",
//        "bestScore": 98.5,
//        "submissions": 3,
//        "submittedAt": "2025-11-07T15:20:35"
//        },
//        {
//        "rank": 2,
//        "userid": "eun",
//        "bestScore": 95.0,
//        "submissions": 2,
//        "submittedAt": "2025-11-07T14:59:12"
//        }
//]