import Layout from './Layout.jsx'
import { Link } from 'react-router-dom';
import '../css/leaderboard.scss'
import { useState } from 'react';

const Leaderboard = () => {
    
    const [leaderboard , setLeaderboard] = useState({});
    const [compNameList , setCompNameList ] = useState({});
    const [keyword , setKeyword] = useState("");
    const [isEmpty , setIsEmpty] = useState(false);



    return (
        <Layout>

            <main className="main">
                <section className="section-wrap">
                    <div>
                        <h1>리더보드 🏆</h1>
                        <p>상위권 참가자의 점수를 확인하세요.</p>
                    </div>

                    <h3>대회이름</h3>
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
                            <tbody className="leaderboardBody">
                                <tr>
                                    <th></th>
                                    <th></th>
                                    <th></th>
                                    <th></th>
                                    <th></th>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </section>
            </main>

        </Layout>


    )
}

export default Leaderboard

